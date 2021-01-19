/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Account {

    @Property()
    private final String accountID;

    // base64 encoded public key
    @Property()
    private final String publicKey;

    @Property()
    private final double balance;

    public String getAccountID() {
        return accountID;
    }

    public double getBalance() {
        return balance;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Account(@JsonProperty("accountID") final String accountID,
                   @JsonProperty("balance") final double balance,
                   @JsonProperty("publicKey") final String publicKey) {
        this.accountID = accountID;
        this.balance = balance;
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Account other = (Account) obj;

        return Objects.equals(getAccountID(), other.getAccountID())
                &&
                Objects.equals(getPublicKey(), other.getPublicKey())
                &&
                Objects.equals(getBalance(), other.getBalance());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountID(), getBalance());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + " [accountID=" + accountID + ", balance=" + balance
                + "]";
    }
}
