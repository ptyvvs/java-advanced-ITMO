package info.kgeorgiy.ja.boguslavskaya.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Client {
    /** Utility class. */
    private Client() {}

    public static void main(final String... args) throws RemoteException {
        final Registry registry = LocateRegistry.getRegistry(2732);
        final Bank bank;
        try {
            bank = (Bank) registry.lookup("server.bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        }
        if (args.length != 5){
            System.out.println("Please retry with this arguments:<name><last_name><passport_id><account_id><amount_changing_sum>");
        }

        final String name = args[0];
        final String lastName = args[1];
        final String passportId = args[2];
        final String accountSubId = args[3];
        final int newAmmount = Integer.parseInt(args[4]);

        Person person = bank.getPerson(passportId, PersonType.REMOTE);
        if (person == null){
            System.out.println("Creating a person");
            person = bank.createPerson(name, lastName, passportId, PersonType.REMOTE);
        } else {
            System.out.println("Person already exists");
        }

        Account account = bank.getAccountByPerson(person, accountSubId);
        if (account == null){
            System.out.println("Creating account");
            account = bank.createAccountOfPerson(accountSubId, person);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Old amount: " + account.getAmount());
        System.out.println("Changing amount of money");
        account.setAmount(newAmmount);
        System.out.println("Person: " + lastName + " " + name);
        System.out.println("Account: " + accountSubId);
        System.out.println("Amount: " + account.getAmount());
    }
}
