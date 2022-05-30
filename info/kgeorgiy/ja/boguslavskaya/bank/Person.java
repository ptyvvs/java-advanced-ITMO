package info.kgeorgiy.ja.boguslavskaya.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    String getName() throws RemoteException;

    String getLastName() throws RemoteException;

    String getPassportID() throws RemoteException;
}
