package org.tron.studio;

import org.tron.abi.datatypes.Function;
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
    private Function function;

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

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public TransactionHistoryItem(Type type, GrpcAPI.TransactionExtention transactionExtention, Function function) {
        this.type = type;
        this.transactionExtention = transactionExtention;
        this.function = function;
    }

    public TransactionHistoryItem(Type type, Protocol.Transaction transaction, Function function) {
        this.type = type;
        this.transaction = transaction;
        this.function = function;
    }

    public TransactionHistoryItem(Type type, String infoString, Function function) {
        this.type = type;
        this.infoString = infoString;
        this.function = function;
    }
}
