/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "Asset Transfer",
                description = "The hyperlegendary asset transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "a.transfer@example.com",
                        name = "Adrian Transfer",
                        url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        ACCOUNT_NOT_FOUND,
        ASSET_ALREADY_EXISTS,
        NOT_ENOUGH_BALANCE
    }

    /**
     * Initialize ledger
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
    }


    /**
     * Create account with initial balance
     * @param ctx
     * @param accountID
     * @param balance
     * @return
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Account CreateAccount(final Context ctx, final String accountID, final double balance, final String publicKey) {
        ChaincodeStub stub = ctx.getStub();

        if (AccountExists(ctx, accountID)) {
            String errorMessage = String.format("Account %s already exists", accountID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Account account = new Account(accountID, balance, publicKey);
        String assetJSON = genson.serialize(account);
        stub.putStringState(accountID, assetJSON);

        return account;
    }

    /**
     * Retrieves an asset with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param accountID the ID of the asset
     * @return the account found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Account ReadAccount(final Context ctx, final String accountID) {
        return getAccountByID(ctx.getStub(), accountID);
    }


    /**
     * Checks the existence of the asset on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AccountExists(final Context ctx, final String accountID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(accountID);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    private Account getAccountByID(final ChaincodeStub stub, final String accountID) throws ChaincodeException {
        String accountJSON = stub.getStringState(accountID);

        if (accountJSON == null || accountJSON.isEmpty()) {
            String errorMessage = String.format("Account %s does not exist", accountID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ACCOUNT_NOT_FOUND.toString());
        }
        return genson.deserialize(accountJSON, Account.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void Send(final Context ctx, final String fromID, final String toID, final double amount) {
        ChaincodeStub stub = ctx.getStub();

        Account from = getAccountByID(stub, fromID);
        Account to = getAccountByID(stub, toID);
        // check balance
        if (from.getBalance() < amount) {
            String errMsg = String.format("Account %s does not have enough blanace to make the transaction", fromID);
            throw new ChaincodeException(errMsg, AssetTransferErrors.NOT_ENOUGH_BALANCE.toString());
        }

        Account newFrom = new Account(fromID, from.getBalance() - amount, from.getPublicKey());
        Account newTo = new Account(toID, to.getBalance() + amount, to.getPublicKey());
        String newFromJson = genson.serialize(newFrom);
        String newToJson = genson.serialize(newTo);
        stub.putStringState(fromID, newFromJson);
        stub.putStringState(toID, newToJson);
    }

    /**
     * Retrieves all assets from the ledger.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAccounts(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Account> queryResults = new ArrayList<>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Account account = genson.deserialize(result.getStringValue(), Account.class);
            queryResults.add(account);
            System.out.println(account.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }
}
