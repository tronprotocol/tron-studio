package org.tron.studio;

import org.tron.api.GrpcAPI;
import org.tron.protos.Protocol;

public class TransactionHistoryItem {

    public enum Type {
        Transaction, TransactionExtension, InfoString
    }

    private Type type;
    private GrpcAPI.TransactionExtention transactionExtention;
    private Protocol.Transaction transaction;
    private String infoString;

    public TransactionHistoryItem(Type type, GrpcAPI.TransactionExtention transactionExtention) {
        this.type = type;
        this.transactionExtention = transactionExtention;
    }

    public TransactionHistoryItem(Type type, Protocol.Transaction transaction) {
        this.type = type;
        this.transaction = transaction;
    }

    public TransactionHistoryItem(Type type, String errorInfo) {
        this.type = type;
        this.infoString = errorInfo;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public GrpcAPI.TransactionExtention getTransactionExtention() {
        return transactionExtention;
    }

    public void setTransactionExtention(GrpcAPI.TransactionExtention transactionExtention) {
        this.transactionExtention = transactionExtention;
    }

    public Protocol.Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Protocol.Transaction transaction) {
        this.transaction = transaction;
    }

    public String getInfoString() {
        return infoString;
    }

    public void setInfoString(String infoString) {
        this.infoString = infoString;
    }
}
