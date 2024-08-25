import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
// import java.util.List;

class BankAccount {
    private String accountNumber;
    private double balance;
    private Lock lock;

    public BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.lock = new ReentrantLock();
    }

    public void deposit(double amount) {
        lock.lock();
        try {
            balance += amount;
            logTransaction("Deposit: +" + amount);
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(double amount) {
        lock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
                logTransaction("Withdrawal: -" + amount);
            } else {
                logTransaction("Withdrawal failed: Insufficient funds");
                throw new ArithmeticException();
            }
        } finally {
            lock.unlock();
        }
    }

    public double getBalance() {
        return balance;
    }

    private void logTransaction(String transaction) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        System.out.println("[" + timestamp + "] Account " + accountNumber + ": " + transaction);
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}

class TransactionThread extends Thread {
    private BankAccount account;
    private boolean isDeposit;
    private double amount;

    public TransactionThread(BankAccount account, boolean isDeposit, double amount) {
        this.account = account;
        this.isDeposit = isDeposit;
        this.amount = amount;
    }

    @Override
    public void run() {
        if (isDeposit) {
            account.deposit(amount);
        } else {
            try {
                account.withdraw(amount);
            } catch (ArithmeticException e) {
                // Handle insufficient funds if needed
                e.printStackTrace();
            }
        }
    }
}

public class BankSystemGUI extends JFrame {
    private BankAccount account;
    private JTextField amountField;
    private static JTextArea transactionArea;
    private JLabel accountNumberLabel;

    public BankSystemGUI(BankAccount account) {
        this.account = account;
        initialize();
    }

    private void initialize() {
        setTitle("Banking System");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        accountNumberLabel = new JLabel("Account Number: " + account.getAccountNumber());
        add(accountNumberLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel amountLabel = new JLabel("Enter Amount:");
        amountField = new JTextField();

        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton checkBalanceButton = new JButton("Check Balance");
        JButton closeButton = new JButton("Close");

        transactionArea = new JTextArea();
        transactionArea.setEditable(false);

        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(depositButton);
        panel.add(withdrawButton);
        panel.add(checkBalanceButton);
        panel.add(closeButton);

        // panel.setBackground(Color.GREEN);
        // depositButton.setForeground(Color.GREEN);
        // withdrawButton.setForeground(Color.RED);
        
        add(panel, BorderLayout.SOUTH);
        add(new JScrollPane(transactionArea), BorderLayout.CENTER);

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performTransaction(true);
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performTransaction(false);
            }
        });

        checkBalanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBalance();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Close the application
            }
        });

        // updateBalance();
    }

    private void showBalance() {
        JOptionPane.showMessageDialog(this, "Current Balance: $" + account.getBalance(), "Balance", JOptionPane.INFORMATION_MESSAGE);
    }

    private void performTransaction(boolean isDeposit) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (isDeposit) {
                account.deposit(amount);
            } else {
                try {
                    account.withdraw(amount);
                } catch (ArithmeticException e) {
                    JOptionPane.showMessageDialog(this, "Insufficient funds", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Stop further processing if there are insufficient funds
                }
            }
            // updateBalance();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // private void updateBalance() {
    //     amountField.setText("");
    //     transactionArea.setText("Transactions:\n");
    //     transactionArea.append("Balance: $" + account.getBalance() + "\n");
    // }

    // public static void appendTransactionLog(String logMessage) {
    //     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //     String timestamp = sdf.format(new Date());
    //     String logWithTimestamp = "[" + timestamp + "] " + logMessage;
    //     transactionArea.append(logWithTimestamp + "\n");
    // }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Number of Accounts: ");
        int n = sc.nextInt();
        int accNo[] = new int[n];
        for(int i = 0; i < n; i++) {
            System.out.println("Enter Account Number: ");
            accNo[i] = sc.nextInt();
        }
        for(int i = 0; i < n; i++) {
            String currAcc="";
            currAcc += accNo[i];
            // String accName = "Acc"+accNo[i];
            BankAccount accName = new BankAccount(currAcc, 0);
            System.out.println("Account with " + accNo[i] + "is Created Successfully.");

            SwingUtilities.invokeLater(() -> {
                new BankSystemGUI(accName).setVisible(true);

                // Create multiple threads for concurrent transactions
                TransactionThread depositThread = new TransactionThread(accName, true, 0);
                TransactionThread withdrawThread = new TransactionThread(accName, false, 0);

                // Start the threads
                depositThread.start();
                withdrawThread.start();
            });
        }
        sc.close() ; 

    }
}