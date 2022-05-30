package info.kgeorgiy.ja.boguslavskaya.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemotePerson implements Person{
    private final String name;
    private final String lastName;
    private final String passportId;

    public RemotePerson(String name, String lastName, String passportId) {
        this.name = name;
        this.lastName = lastName;
        this.passportId = passportId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportID() {
        return passportId;
    }

}
