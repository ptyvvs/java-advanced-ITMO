package info.kgeorgiy.ja.boguslavskaya.bank;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@DisplayName("Bank Tests")
public class BankTest {

    private static Bank bank;
    private static final String[] names = {"Татьяна", "Анна", "Никита", "Владислав"};
    private static final String[] lastNames = {"Богуславская", "Лоренц", "Бондарев", "Гринь"};
    private static final String[] passportIds = {"2205", "2109", "2404", "0901"};



    @BeforeAll
    static void beforeAll(){
        serverSetup();
        startBankTesting();
    }

    static void serverSetup() {
        bank = new RemoteBank(2732);
        try {
            final Registry registry = LocateRegistry.createRegistry(2732);
            UnicastRemoteObject.exportObject(bank, 0);
            registry.bind("server.bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final AlreadyBoundException e) {
            System.out.println("Malformed URL");
        }
    }

    @DisplayName("Bank creation")
    static void startBankTesting()  {
        try {
            final Registry registry = LocateRegistry.getRegistry(2732);
            bank = (Bank) registry.lookup("server.bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
        } catch (RemoteException e){
            System.out.println("Remote problems");
        }
    }

    @Test
    @DisplayName("Test for remote and local person creation")
    void checkPersonsCreation() {
        for (int i = 0; i < passportIds.length; i++){
            try{
                Person remotePerson = bank.createPerson(names[i], lastNames[i], passportIds[i], PersonType.REMOTE);
                Person localPerson = bank.createPerson(names[i], lastNames[i], passportIds[i], PersonType.LOCAL);
                checkAssertForPerson(remotePerson, names[i], lastNames[i], passportIds[i]);
                checkAssertForPerson(localPerson, names[i], lastNames[i], passportIds[i]);
            } catch (RemoteException e){
                System.out.println("Problems during creating remote person");
            }
        }
    }

    @Test
    @DisplayName("Test for creation local persons and changing amount")
    void test2() throws RemoteException {
        for (int i = 0; i < passportIds.length; i++){
            Person remotePerson = bank.createPerson(names[i], lastNames[i], passportIds[i], PersonType.REMOTE);
            checkAssertForPerson(remotePerson, names[i], lastNames[i], passportIds[i]);
            Account remoteAccount = bank.createAccountOfPerson(String.valueOf(i), remotePerson);
            remoteAccount.setAmount(i * 2);
            Person localPerson = bank.getPerson(passportIds[i], PersonType.LOCAL);
            Account localAccount = bank.getAccountByPerson(localPerson, String.valueOf(i));
            localAccount.setAmount(i * 4);
            Assert.assertEquals(i * 2, remoteAccount.getAmount());
        }
    }

    @Test
    @DisplayName("Test to change amount of remote person")
    void test() throws RemoteException {
        for (int i = 0; i < passportIds.length; i++){
            Person person = bank.createPerson(names[i], lastNames[i], passportIds[i], PersonType.REMOTE);
            Account account = bank.createAccountOfPerson("1", person);
            account.setAmount(12);
            account.changeAmount(50);
            account.changeAmount(-8);
            checkAssertForPerson(person, names[i], lastNames[i], passportIds[i]);
            Assert.assertEquals(54, account.getAmount());
        }
    }

    @Test
    @DisplayName("Test to concurrent work")
    void concurrentTest() {
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < passportIds.length; i++){
            final int finalI = i;
            threadPool.submit(() -> {
                try {
                    Person person = bank.createPerson(names[finalI], lastNames[finalI], passportIds[finalI], PersonType.REMOTE);
                    Account account = bank.createAccountOfPerson("1", person);
                    if (person == null || account == null){
                        throw new AssertionError("person or account == null");
                    }
                    checkAssertForPerson(person, names[finalI],lastNames[finalI], passportIds[finalI]);
                    account.setAmount(finalI);
                    System.out.println(account.getAmount());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination( 100, TimeUnit.MINUTES);
        } catch (InterruptedException e){
            System.err.println(e.getMessage());
        }
    }

    @DisplayName("Check for equals of person and expected parameters")
    void checkAssertForPerson(Person person, String name, String lastName, String passportId) throws RemoteException {
        Assert.assertEquals(name, person.getName());
        Assert.assertEquals(lastName, person.getLastName());
        Assert.assertEquals(passportId, person.getPassportID());
    }

}
