package com.app.product.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.app.product.dto.CreateProductRequest;
import com.app.product.dto.InterestRateMatrixRequest;
import com.app.product.dto.ProductChargeRequest;
import com.app.product.dto.ProductRoleRequest;
import com.app.product.entity.CustomerCommunication;
import com.app.product.entity.Product;
import com.app.product.entity.ProductBalanceType;
import com.app.product.entity.ProductTransactionType;
import com.app.product.entity.TransactionBalanceRelationship;
import com.app.product.enums.BalanceType;
import com.app.product.enums.ChargeFrequency;
import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;
import com.app.product.enums.RoleType;
import com.app.product.enums.TransactionType;
import com.app.product.repository.CustomerCommunicationRepository;
import com.app.product.repository.ProductBalanceTypeRepository;
import com.app.product.repository.ProductRepository;
import com.app.product.repository.ProductTransactionTypeRepository;
import com.app.product.repository.TransactionBalanceRelationshipRepository;
import com.app.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Data Initialiser - Loads default FD products and reference data
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductService productService;
    private final TransactionBalanceRelationshipRepository transactionBalanceRelationshipRepository;
    private final CustomerCommunicationRepository customerCommunicationRepository;
    private final ProductTransactionTypeRepository productTransactionTypeRepository;
    private final ProductBalanceTypeRepository productBalanceTypeRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        // Initialize reference data first
        initializeReferenceData();
        
        // Check and create products if needed
        boolean productsCreated = initializeProducts();
        
        // Backfill customer communications and transaction types for existing products
        // (including newly created ones)
        if (productsCreated || productRepository.count() > 0) {
            backfillProductData();
        }
        
        log.info("Data initialization completed successfully");
    }

    /**
     * Initialize all reference/lookup tables
     */
    private void initializeReferenceData() {
        log.info("Checking if reference data exists...");
        
        if (transactionBalanceRelationshipRepository.count() > 0) {
            log.info("Reference data already exists. Skipping initialization.");
            return;
        }
        
        try {
            // Note: CustomerCommunication and ProductTransactionType have product_id FK
            // These will be created per product, not as reference data
            
            // Only initialize truly independent reference data
            initializeTransactionBalanceRelationships();
            
            log.info("Successfully loaded all reference data");
        } catch (Exception e) {
            log.error("Error loading reference data: {}", e.getMessage(), e);
        }
    }

    /**
     * Backfill CustomerCommunication, ProductTransactionType, and ProductBalanceType for existing products
     */
    private void backfillProductData() {
        log.info("Checking if product-specific data needs backfilling...");
        
        try {
            List<Product> allProducts = productRepository.findAll();
            
            if (allProducts.isEmpty()) {
                log.info("No products found. Skipping backfill.");
                return;
            }
            
            for (Product product : allProducts) {
                // Check if communications already exist for this product
                long commCount = customerCommunicationRepository.countByProduct(product);
                if (commCount == 0) {
                    log.info("Creating communications for product: {}", product.getProductName());
                    createCommunicationsForProduct(product);
                } else {
                    log.info("Communications already exist for product: {} (count: {})", product.getProductName(), commCount);
                }
                
                // Check if transaction types already exist for this product
                long txnCount = productTransactionTypeRepository.countByProduct(product);
                if (txnCount == 0) {
                    log.info("Creating transaction types for product: {}", product.getProductName());
                    createTransactionTypesForProduct(product);
                } else {
                    log.info("Transaction types already exist for product: {} (count: {})", product.getProductName(), txnCount);
                }
                
                // Check if balance types already exist for this product
                long balanceCount = productBalanceTypeRepository.countByProduct(product);
                if (balanceCount == 0) {
                    log.info("Creating balance types for product: {}", product.getProductName());
                    createBalanceTypesForProduct(product);
                } else {
                    log.info("Balance types already exist for product: {} (count: {})", product.getProductName(), balanceCount);
                }
            }
            
            log.info("Product data backfill completed");
        } catch (Exception e) {
            log.error("Error during product data backfill: {}", e.getMessage(), e);
        }
    }

    /**
     * Initialize FD products
     * @return 
     */
    /**
     * Initialize FD products
     * @return true if products were created, false if they already existed
     */
    private boolean initializeProducts() {
        log.info("Checking if default FD products exist...");
        
        if (productService.getAllProducts(0, 1, "id", "ASC").getTotalElements() > 0) {
            log.info("Products already exist in database. Skipping initialization.");
            return false;
        }
        
        log.info("Loading default FD products...");
        
        try {
            createStandardFixedDeposit();
            createSeniorCitizenFD();
            createTaxSaverFD();
            createCumulativeFD();
            createNonCumulativeFD();
            createFlexiFD();
            
            log.info("Successfully loaded {} default FD products", 6);
            return true;
        } catch (Exception e) {
            log.error("Error loading default FD products: {}", e.getMessage(), e);
            return false;
        }
    }

    // ============================================================================
    // REFERENCE DATA INITIALIZATION
    // ============================================================================

    /**
     * Initialize Transaction Balance Relationships
     */
    private void initializeTransactionBalanceRelationships() {
        log.info("Initializing transaction-balance relationships...");
        
        List<TransactionBalanceRelationship> relationships = new ArrayList<>();
        
        // Create relationships and filter out nulls (for unmapped enums)
        TransactionBalanceRelationship[] relations = {
            // FD Opening Deposit
            createRelationship("FD_OPENING_DEPOSIT", "PRINCIPAL", "CREDIT", "Deposit principal amount"),
            createRelationship("FD_OPENING_DEPOSIT", "AVAILABLE", "CREDIT", "Initialize available balance"),
            
            // Interest Accrued
            createRelationship("INTEREST_ACCRUED", "INTEREST_ACCRUED", "CREDIT", "Accrue interest"),
            
            // Interest Payout
            createRelationship("INTEREST_PAYOUT", "INTEREST_ACCRUED", "DEBIT", "Pay out accrued interest"),
            createRelationship("INTEREST_PAYOUT", "INTEREST_PAID", "CREDIT", "Track paid interest"),
            
            // Interest Credit
            createRelationship("INTEREST_CREDIT", "INTEREST_ACCRUED", "DEBIT", "Transfer accrued interest"),
            createRelationship("INTEREST_CREDIT", "INTEREST_PAID", "CREDIT", "Credit interest to account"),
            
            // Maturity Withdrawal
            createRelationship("MATURITY_WITHDRAWAL", "PRINCIPAL", "DEBIT", "Pay out principal"),
            createRelationship("MATURITY_WITHDRAWAL", "INTEREST_ACCRUED", "DEBIT", "Pay out interest"),
            createRelationship("MATURITY_WITHDRAWAL", "AVAILABLE", "DEBIT", "Zero out balance"),
            
            // Premature Withdrawal
            createRelationship("PREMATURE_WITHDRAWAL", "PRINCIPAL", "DEBIT", "Withdraw principal"),
            createRelationship("PREMATURE_WITHDRAWAL", "INTEREST_ACCRUED", "DEBIT", "Withdraw interest"),
            createRelationship("PREMATURE_WITHDRAWAL", "AVAILABLE", "DEBIT", "Reduce available balance"),
            
            // Partial Withdrawal
            createRelationship("PARTIAL_WITHDRAWAL", "AVAILABLE", "DEBIT", "Partial withdrawal"),
            createRelationship("PARTIAL_WITHDRAWAL", "PRINCIPAL", "DEBIT", "Reduce principal"),
            
            // Penalty Charge
            createRelationship("PENALTY_CHARGE", "CHARGES", "CREDIT", "Apply penalty"),
            createRelationship("PENALTY_CHARGE", "AVAILABLE", "DEBIT", "Deduct penalty from balance"),
            createRelationship("PENALTY_CHARGE", "INTEREST_ACCRUED", "DEBIT", "Reduce interest for penalty"),
            
            // TDS Deduction
            createRelationship("TDS_DEDUCTION", "CHARGES", "CREDIT", "Withhold TDS"),
            createRelationship("TDS_DEDUCTION", "INTEREST_ACCRUED", "DEBIT", "Deduct TDS from interest"),
            
            // Service Charge
            createRelationship("SERVICE_CHARGE", "CHARGES", "CREDIT", "Apply service charges"),
            createRelationship("SERVICE_CHARGE", "AVAILABLE", "DEBIT", "Deduct charges"),
            
            // Processing Fee
            createRelationship("PROCESSING_FEE", "CHARGES", "CREDIT", "Apply processing fee"),
            createRelationship("PROCESSING_FEE", "AVAILABLE", "DEBIT", "Deduct fee"),
            
            // Loan Disbursement
            createRelationship("LOAN_DISBURSEMENT", "PRINCIPAL", "CREDIT", "Loan principal outstanding"),
            createRelationship("LOAN_DISBURSEMENT", "AVAILABLE", "CREDIT", "Track disbursement amount"),
            
            // Loan Payment
            createRelationship("LOAN_PAYMENT", "PRINCIPAL", "DEBIT", "Reduce loan principal"),
            createRelationship("LOAN_PAYMENT", "INTEREST_ACCRUED", "DEBIT", "Pay loan interest"),
            
            // Loan EMI Payment
            createRelationship("LOAN_EMI_PAYMENT", "PRINCIPAL", "DEBIT", "EMI principal component"),
            createRelationship("LOAN_EMI_PAYMENT", "INTEREST_ACCRUED", "DEBIT", "EMI interest component"),
            
            // Auto Renewal
            createRelationship("AUTO_RENEWAL", "PRINCIPAL", "CREDIT", "Renew principal"),
            createRelationship("AUTO_RENEWAL", "INTEREST_ACCRUED", "DEBIT", "Reset interest for new term"),
            
            // Manual Renewal
            createRelationship("MANUAL_RENEWAL", "PRINCIPAL", "CREDIT", "Renew principal amount"),
            createRelationship("MANUAL_RENEWAL", "INTEREST_ACCRUED", "DEBIT", "Clear old interest"),
            createRelationship("MANUAL_RENEWAL", "AVAILABLE", "CREDIT", "Reset available balance")
        };
        
        // Filter out null values (for unmapped transaction/balance types)
        for (TransactionBalanceRelationship relation : relations) {
            if (relation != null) {
                relationships.add(relation);
            }
        }
        
        if (!relationships.isEmpty()) {
            transactionBalanceRelationshipRepository.saveAll(relationships);
            log.info("Created {} transaction-balance relationships", relationships.size());
        } else {
            log.warn("No transaction-balance relationships were created. Check enum mapping.");
        }
    }

    // ============================================================================
    // HELPER METHODS FOR CREATING ENTITIES
    // ============================================================================

    private TransactionBalanceRelationship createRelationship(String transactionTypeCode, String balanceTypeCode,
            String impactType, String description) {
        try {
            TransactionBalanceRelationship relationship = new TransactionBalanceRelationship();
            relationship.setTransactionType(TransactionType.valueOf(transactionTypeCode));
            relationship.setBalanceType(BalanceType.valueOf(balanceTypeCode));
            relationship.setImpactType(impactType);
            relationship.setDescription(description);
            relationship.setActive(true);
            return relationship;
        } catch (IllegalArgumentException e) {
            log.warn("Enum not found for TransactionType: {} or BalanceType: {}, skipping relationship", 
                transactionTypeCode, balanceTypeCode);
            return null;
        }
    }

    /**
     * Create default customer communications for a product
     * Covers: Alerts, Notices, Statements, Transaction confirmations
     */
    private void createCommunicationsForProduct(Product product) {
        List<CustomerCommunication> communications = Arrays.asList(
            // ALERTS - Account Opening
            createCommunication(product, "EMAIL", "ACCOUNT_OPENING", 
                "Fixed Deposit Account Opening Confirmation",
                "Dear {customerName}, Your Fixed Deposit account {accountNumber} has been successfully opened. Deposit Amount: {amount}, Tenure: {tenure} months, Interest Rate: {interestRate}%, Maturity Date: {maturityDate}. Thank you for choosing us.",
                true, true),
            createCommunication(product, "SMS", "ACCOUNT_OPENING",
                "FD Opening SMS",
                "Your FD A/c {accountNumber} opened successfully. Amount: {amount}, Rate: {interestRate}%, Maturity: {maturityDate}",
                true, true),
            
            // NOTICES - FD Receipt
            createCommunication(product, "EMAIL", "FD_RECEIPT",
                "FD Receipt - Account Opening",
                "Dear {customerName}, Please find attached your Fixed Deposit Receipt for account {accountNumber}. Deposit Amount: {amount}, Interest Rate: {interestRate}%, Maturity Date: {maturityDate}.",
                true, false),
            
            // NOTICES - Maturity Notice
            createCommunication(product, "EMAIL", "FD_MATURITY_NOTICE",
                "Fixed Deposit Maturity Notice",
                "Dear {customerName}, Your Fixed Deposit {accountNumber} will mature on {maturityDate}. Maturity Amount: {maturityAmount}. Please visit our branch or login to renew or withdraw.",
                true, true),
            createCommunication(product, "SMS", "FD_MATURITY_NOTICE",
                "FD Maturity SMS",
                "Your FD {accountNumber} matures on {maturityDate}. Maturity Amt: {maturityAmount}. Renew or withdraw at branch.",
                true, true),
            
            // NOTICES - Loan Payment Due
            createCommunication(product, "EMAIL", "LOAN_PAYMENT_DUE",
                "Loan Against FD - Payment Due Notice",
                "Dear {customerName}, Your loan payment of Rs.{paymentAmount} is due on {dueDate} for loan against FD {accountNumber}. Please ensure timely payment.",
                true, true),
            createCommunication(product, "SMS", "LOAN_PAYMENT_DUE",
                "Loan Payment Due SMS",
                "Loan payment Rs.{paymentAmount} due on {dueDate} for FD {accountNumber}. Pay now to avoid penalty.",
                true, true),
            
            // STATEMENTS - Recurring Statement
            createCommunication(product, "EMAIL", "QUARTERLY_STATEMENT",
                "Quarterly FD Statement",
                "Dear {customerName}, Please find attached your quarterly statement for FD {accountNumber} for the period {periodStart} to {periodEnd}.",
                true, false),
            createCommunication(product, "EMAIL", "ANNUAL_STATEMENT",
                "Annual FD Statement",
                "Dear {customerName}, Please find attached your annual statement for FD {accountNumber} for the financial year {year}.",
                true, false),
            
            // STATEMENTS - Ad-hoc Statement
            createCommunication(product, "EMAIL", "ADHOC_STATEMENT",
                "FD Statement - On Demand",
                "Dear {customerName}, Please find attached your requested statement for FD {accountNumber} for the period {periodStart} to {periodEnd}.",
                true, false),
            
            // TRANSACTION ALERTS - Deposit
            createCommunication(product, "SMS", "DEPOSIT_CONFIRMATION",
                "Deposit Confirmation",
                "Deposit of Rs.{amount} successful in FD {accountNumber} on {transactionDate}. Available Balance: {balance}",
                true, true),
            
            // TRANSACTION ALERTS - Withdrawal
            createCommunication(product, "EMAIL", "WITHDRAWAL_CONFIRMATION",
                "Withdrawal Confirmation",
                "Dear {customerName}, Withdrawal of Rs.{amount} from FD {accountNumber} has been processed on {transactionDate}. Remaining Balance: {balance}",
                true, true),
            createCommunication(product, "SMS", "WITHDRAWAL_CONFIRMATION",
                "Withdrawal SMS",
                "Withdrawal of Rs.{amount} from FD {accountNumber} successful on {transactionDate}. Balance: {balance}",
                true, true),
            
            // TRANSACTION ALERTS - Interest Accrued
            createCommunication(product, "EMAIL", "INTEREST_ACCRUED",
                "Interest Accrued Notification",
                "Dear {customerName}, Interest of Rs.{interestAmount} has accrued on your FD {accountNumber} for the period {periodStart} to {periodEnd}.",
                true, false),
            
            // TRANSACTION ALERTS - Interest Credit
            createCommunication(product, "EMAIL", "INTEREST_CREDIT",
                "Interest Credit Notification",
                "Dear {customerName}, Interest of Rs.{interestAmount} has been credited to your account for FD {accountNumber}. Transaction Date: {transactionDate}",
                true, false),
            createCommunication(product, "SMS", "INTEREST_CREDIT",
                "Interest Credit SMS",
                "Interest Rs.{interestAmount} credited to FD {accountNumber} on {transactionDate}",
                true, false),
            
            // TRANSACTION ALERTS - Disbursement (Loan Against FD)
            createCommunication(product, "EMAIL", "LOAN_DISBURSEMENT",
                "Loan Disbursement Confirmation",
                "Dear {customerName}, Loan of Rs.{loanAmount} against your FD {accountNumber} has been disbursed to account {disbursementAccount} on {transactionDate}.",
                true, true),
            createCommunication(product, "SMS", "LOAN_DISBURSEMENT",
                "Loan Disbursement SMS",
                "Loan Rs.{loanAmount} disbursed against FD {accountNumber} on {transactionDate}",
                true, true),
            
            // TRANSACTION ALERTS - Payment (Loan Repayment)
            createCommunication(product, "EMAIL", "LOAN_PAYMENT",
                "Loan Payment Confirmation",
                "Dear {customerName}, Payment of Rs.{paymentAmount} received for loan against FD {accountNumber}. Outstanding: Rs.{outstandingAmount}",
                true, false),
            createCommunication(product, "SMS", "LOAN_PAYMENT",
                "Loan Payment SMS",
                "Payment Rs.{paymentAmount} received for loan on FD {accountNumber}. Outstanding: Rs.{outstandingAmount}",
                true, false),
            
            // TRANSACTION ALERTS - Premature Withdrawal
            createCommunication(product, "EMAIL", "PREMATURE_WITHDRAWAL",
                "Premature Withdrawal Confirmation",
                "Dear {customerName}, Your request for premature withdrawal of FD {accountNumber} has been processed. Withdrawal Amount: {withdrawalAmount}, Penalty: {penaltyAmount}, Net Amount Credited: {netAmount}",
                true, true),
            
            // TRANSACTION ALERTS - Auto Renewal
            createCommunication(product, "EMAIL", "AUTO_RENEWAL",
                "Auto Renewal Notification",
                "Dear {customerName}, Your Fixed Deposit {accountNumber} has been auto-renewed. Renewal Amount: {renewalAmount}, New Maturity Date: {newMaturityDate}, Interest Rate: {newInterestRate}%",
                true, true),
            
            // TRANSACTION ALERTS - TDS Deduction
            createCommunication(product, "EMAIL", "TDS_DEDUCTION",
                "TDS Deduction Notice",
                "Dear {customerName}, TDS of Rs.{tdsAmount} has been deducted from your FD interest. Gross Interest: {interestAmount}, TDS Deducted: {tdsAmount}, Net Interest: {netAmount}",
                true, false),
            
            // Account Closure
            createCommunication(product, "EMAIL", "ACCOUNT_CLOSURE",
                "Fixed Deposit Account Closure Confirmation",
                "Dear {customerName}, Your Fixed Deposit {accountNumber} has been closed successfully. Final Settlement Amount: {settlementAmount}",
                true, true),
            createCommunication(product, "SMS", "ACCOUNT_CLOSURE",
                "Account Closure SMS",
                "FD {accountNumber} closed successfully. Settlement amount Rs.{settlementAmount} credited to your account.",
                true, true)
        );
        
        customerCommunicationRepository.saveAll(communications);
        log.info("Created {} communications for product: {}", communications.size(), product.getProductName());
    }

    private CustomerCommunication createCommunication(Product product, String type, String event, 
            String template, String content, boolean active, boolean mandatory) {
        return CustomerCommunication.builder()
                .product(product)
                .communicationType(type)
                .event(event)
                .template(template)
                .subject(template)
                .content(content)
                .active(active)
                .mandatory(mandatory)
                .build();
    }

    /**
     * Create default transaction types for a product
     * Covers: Deposit, Withdrawal, Interest Accrued, Disbursement, Payment
     */
    private void createTransactionTypesForProduct(Product product) {
        List<ProductTransactionType> transactionTypes = new ArrayList<>();
        
        // Define all transaction types with their properties
        String[][] txnData = {
            // Deposit transactions
            {"FD_OPENING_DEPOSIT", "Fixed Deposit Opening - Initial deposit", "false"},
            {"ADDITIONAL_DEPOSIT", "Additional Deposit to FD", "false"},
            
            // Withdrawal transactions
            {"MATURITY_WITHDRAWAL", "Withdrawal at Maturity", "false"},
            {"PREMATURE_WITHDRAWAL", "Premature Withdrawal before maturity", "true"},
            {"PARTIAL_WITHDRAWAL", "Partial Withdrawal", "true"},
            
            // Interest transactions
            {"INTEREST_ACCRUED", "Interest Accrued on FD", "false"},
            {"INTEREST_PAYOUT", "Interest Payout", "false"},
            {"INTEREST_CREDIT", "Interest Credit to linked account", "false"},
            
            // Disbursement (Loan against FD)
            {"LOAN_DISBURSEMENT", "Loan Disbursement against FD", "true"},
            
            // Payment (Loan repayment)
            {"LOAN_PAYMENT", "Loan Payment", "false"},
            {"LOAN_EMI_PAYMENT", "Loan EMI Payment", "false"},
            {"LOAN_PREPAYMENT", "Loan Pre-payment", "false"},
            {"LOAN_CLOSURE_PAYMENT", "Loan Closure Payment", "false"},
            
            // Charges
            {"PENALTY_CHARGE", "Penalty Charge", "false"},
            {"TDS_DEDUCTION", "TDS Deduction from interest", "false"},
            {"SERVICE_CHARGE", "Service Charge", "false"},
            {"PROCESSING_FEE", "Processing Fee", "false"},
            
            // Renewal
            {"AUTO_RENEWAL", "Auto Renewal of FD", "false"},
            {"MANUAL_RENEWAL", "Manual Renewal of FD", "false"},
            
            // Adjustments
            {"REVERSAL", "Transaction Reversal", "true"},
            {"ADJUSTMENT", "Balance Adjustment", "true"},
            {"CORRECTION", "Transaction Correction", "true"}
        };
        
        for (String[] data : txnData) {
            try {
                ProductTransactionType txnType = ProductTransactionType.builder()
                        .product(product)
                        .transactionType(TransactionType.valueOf(data[0]))
                        .description(data[1])
                        .allowed(true)
                        .requiresApproval(Boolean.parseBoolean(data[2]))
                        .build();
                transactionTypes.add(txnType);
            } catch (IllegalArgumentException e) {
                log.warn("TransactionType enum not found for: {}, skipping", data[0]);
            }
        }
        
        if (!transactionTypes.isEmpty()) {
            productTransactionTypeRepository.saveAll(transactionTypes);
            log.info("Created {} transaction types for product: {}", transactionTypes.size(), product.getProductName());
        }
    }

    /**
     * Create default balance types for a product
     * Covers: FD Principal, Interest, Loan balances
     */
    private void createBalanceTypesForProduct(Product product) {
        List<ProductBalanceType> balanceTypes = new ArrayList<>();
        
        // Define all balance types
        String[][] balanceData = {
            // FD Balances
            {"PRINCIPAL", "FD Principal Balance - Main deposit amount", "true"},
            {"INTEREST_ACCRUED", "FD Interest Accrued - Accumulated interest", "true"},
            {"INTEREST_PAID", "FD Interest Paid - Interest already paid out", "false"},
            {"AVAILABLE", "FD Available Balance - Balance available for operations", "true"},
            
            // Loan Balances (for Loan Against FD)
            {"LOAN_PRINCIPAL", "Loan Principal Outstanding - Loan amount outstanding", "true"},
            {"LOAN_INTEREST", "Loan Interest Outstanding - Interest on loan", "true"},
            
            // Charges
            {"CHARGES", "Charges and Penalties - All charges including TDS, penalties", "false"}
        };
        
        for (String[] data : balanceData) {
            try {
                ProductBalanceType balanceType = ProductBalanceType.builder()
                        .product(product)
                        .balanceType(BalanceType.valueOf(data[0]))
                        .description(data[1])
                        .tracked(Boolean.parseBoolean(data[2]))
                        .build();
                balanceTypes.add(balanceType);
            } catch (IllegalArgumentException e) {
                log.warn("BalanceType enum not found for: {}, skipping", data[0]);
            }
        }
        
        if (!balanceTypes.isEmpty()) {
            productBalanceTypeRepository.saveAll(balanceTypes);
            log.info("Created {} balance types for product: {}", balanceTypes.size(), product.getProductName());
        }
    }

    // ============================================================================
    // PRODUCT INITIALIZATION
    // ============================================================================

    /**
     * Standard customer category and loyalty tier additional rates
     * These rates are applicable across all products
     * Base interest rate should match the product's base rate
     */
    private List<InterestRateMatrixRequest> getStandardCategoryRates(BigDecimal baseRate) {
        List<InterestRateMatrixRequest> rates = new ArrayList<>();
        
        // CUSTOMER CATEGORY RATES
        rates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(100000000))
                .minTermMonths(BigDecimal.valueOf(1))
                .maxTermMonths(BigDecimal.valueOf(1200))
                .customerClassification("SENIOR_CITIZEN")
                .interestRate(baseRate)
                .additionalRate(BigDecimal.valueOf(1.00))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Additional 1% for Senior Citizens (60+ years)")
                .build());
        
        rates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(100000000))
                .minTermMonths(BigDecimal.valueOf(1))
                .maxTermMonths(BigDecimal.valueOf(1200))
                .customerClassification("EMPLOYEE")
                .interestRate(baseRate)
                .additionalRate(BigDecimal.valueOf(1.50))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Additional 1.5% for Bank Employees")
                .build());
        
        // LOYALTY TIER RATES
        rates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(100000000))
                .minTermMonths(BigDecimal.valueOf(1))
                .maxTermMonths(BigDecimal.valueOf(1200))
                .customerClassification("SILVER")
                .interestRate(baseRate)
                .additionalRate(BigDecimal.valueOf(0.50))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Additional 0.5% for Silver tier loyalty")
                .build());
        
        rates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(100000000))
                .minTermMonths(BigDecimal.valueOf(1))
                .maxTermMonths(BigDecimal.valueOf(1200))
                .customerClassification("GOLD")
                .interestRate(baseRate)
                .additionalRate(BigDecimal.valueOf(1.00))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Additional 1% for Gold tier loyalty")
                .build());
        
        rates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(100000000))
                .minTermMonths(BigDecimal.valueOf(1))
                .maxTermMonths(BigDecimal.valueOf(1200))
                .customerClassification("PLATINUM")
                .interestRate(baseRate)
                .additionalRate(BigDecimal.valueOf(1.50))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Additional 1.5% for Platinum tier loyalty")
                .build());
        
        return rates;
    }

    private void createStandardFixedDeposit() {
        log.info("Creating Standard Fixed Deposit...");
        
        BigDecimal baseRate = BigDecimal.valueOf(6.5);
        List<InterestRateMatrixRequest> allRates = new ArrayList<>();
        
        // Product-specific rates
        allRates.addAll(Arrays.asList(
            InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(10000))
                .maxAmount(BigDecimal.valueOf(100000))
                .minTermMonths(BigDecimal.valueOf(6))
                .maxTermMonths(BigDecimal.valueOf(12))
                .interestRate(baseRate)
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .build(),
            InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(1000000))
                .minTermMonths(BigDecimal.valueOf(6))
                .maxTermMonths(BigDecimal.valueOf(12))
                .interestRate(BigDecimal.valueOf(7.0))
                .additionalRate(BigDecimal.valueOf(0.25))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Premium rate for high-value deposits")
                .build(),
            InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(10000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(60))
                .interestRate(BigDecimal.valueOf(7.5))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .build()
        ));
        
        // Add standard category rates with base rate
        allRates.addAll(getStandardCategoryRates(baseRate));
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Standard Fixed Deposit")
                .productCode("FD-STD-001")
                .productType(ProductType.FIXED_DEPOSIT)
                .description("Standard FD with flexible tenure from 6 to 60 months. Suitable for all customers seeking safe returns.")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(6))
                .maxTermMonths(BigDecimal.valueOf(60))
                .minAmount(BigDecimal.valueOf(10000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minBalanceRequired(BigDecimal.valueOf(10000))
                .baseInterestRate(baseRate)
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(true)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Primary account holder")
                        .build(),
                    ProductRoleRequest.builder()
                        .roleType(RoleType.NOMINEE)
                        .mandatory(false)
                        .minCount(0)
                        .maxCount(2)
                        .description("Beneficiary in case of death")
                        .build()
                ))
                
                .charges(Arrays.asList(
                    ProductChargeRequest.builder()
                        .chargeName("Premature Withdrawal Penalty")
                        .chargeType("PENALTY")
                        .description("1% penalty for early withdrawal")
                        .percentageRate(BigDecimal.valueOf(1.0))
                        .frequency(ChargeFrequency.ONE_TIME)
                        .waivable(true)
                        .build()
                ))
                
                .interestRateMatrix(allRates)
                .build();
        
        productService.createProduct(request);
        log.info("Created: Standard Fixed Deposit with {} rate slabs", allRates.size());
    }

    private void createSeniorCitizenFD() {
        log.info("Creating Senior Citizen Fixed Deposit...");
    
        BigDecimal baseRate = BigDecimal.valueOf(7.5);
        List<InterestRateMatrixRequest> allRates = new ArrayList<>();

        allRates.addAll(Arrays.asList(
            InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(25000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(36))
                .interestRate(BigDecimal.valueOf(8.0))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Base rate for 1-3 year tenure")
                .build(),
            InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(25000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minTermMonths(BigDecimal.valueOf(36))
                .maxTermMonths(BigDecimal.valueOf(120))
                .interestRate(BigDecimal.valueOf(8.5))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Base rate for longer tenure")
                .build()
        ));
    
        allRates.addAll(getStandardCategoryRates(baseRate));
    
        CreateProductRequest request = CreateProductRequest.builder()
            .productName("Senior Citizen Fixed Deposit")
            .productCode("FD-SR-001")
            .productType(ProductType.SENIOR_CITIZEN_FD)
            .description("Special FD for senior citizens (60+ years) with higher interest rates and quarterly payouts")
            .effectiveDate(LocalDate.of(2025, 1, 1))
            .bankBranchCode("HEAD")
            .currencyCode("INR")
            .status(ProductStatus.ACTIVE)
            
            .minTermMonths(BigDecimal.valueOf(12))
            .maxTermMonths(BigDecimal.valueOf(120))
            .minAmount(BigDecimal.valueOf(25000))
            .maxAmount(BigDecimal.valueOf(10000000))
            .minBalanceRequired(BigDecimal.valueOf(25000))
            .baseInterestRate(baseRate)
            .interestCalculationMethod("COMPOUND")
            .interestPayoutFrequency("QUARTERLY")
            
            .prematureWithdrawalAllowed(true)
            .partialWithdrawalAllowed(false)
            .loanAgainstDepositAllowed(true)
            .autoRenewalAllowed(true)
            .nomineeAllowed(true)
            .jointAccountAllowed(true)
            
            .tdsApplicable(false)
            
            .allowedRoles(Arrays.asList(
                ProductRoleRequest.builder()
                    .roleType(RoleType.OWNER)
                    .mandatory(true)
                    .minCount(1)
                    .maxCount(1)
                    .description("Senior citizen account holder (60+ years)")
                    .build(),
                ProductRoleRequest.builder()
                    .roleType(RoleType.NOMINEE)
                    .mandatory(true)
                    .minCount(1)
                    .maxCount(2)
                    .description("Mandatory nominee for senior citizen accounts")
                    .build()
            ))
            
            .interestRateMatrix(allRates)
            .build();
    
        productService.createProduct(request);
        log.info("Created: Senior Citizen Fixed Deposit with {} rate slabs", allRates.size());
    }

    private void createTaxSaverFD() {
        log.info("Creating Tax Saver Fixed Deposit...");
        
        BigDecimal baseRate = BigDecimal.valueOf(7.0);
        List<InterestRateMatrixRequest> allRates = new ArrayList<>();
        
        allRates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(10000))
                .maxAmount(BigDecimal.valueOf(1500000))
                .minTermMonths(BigDecimal.valueOf(60))
                .maxTermMonths(BigDecimal.valueOf(61))
                .interestRate(baseRate)
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Fixed 5-year tenure for Section 80C tax benefits")
                .build());
        
        allRates.addAll(getStandardCategoryRates(baseRate));
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Tax Saver Fixed Deposit")
                .productCode("FD-TAX-001")
                .productType(ProductType.TAX_SAVER_FD)
                .description("5-year lock-in FD with tax benefits under Section 80C (up to ₹1.5 Lakh)")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(60))
                .maxTermMonths(BigDecimal.valueOf(61))
                .minAmount(BigDecimal.valueOf(10000))
                .maxAmount(BigDecimal.valueOf(1500000))
                .minBalanceRequired(BigDecimal.valueOf(10000))
                .baseInterestRate(baseRate)
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(false)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(false)
                .autoRenewalAllowed(false)
                .nomineeAllowed(true)
                .jointAccountAllowed(false)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Individual account holder only (no joint accounts)")
                        .build()
                ))
                
                .interestRateMatrix(allRates)
                .build();
        
        productService.createProduct(request);
        log.info("Created: Tax Saver Fixed Deposit with {} rate slabs", allRates.size());
    }

    private void createCumulativeFD() {
        log.info("Creating Cumulative Fixed Deposit...");
        
        BigDecimal baseRate = BigDecimal.valueOf(6.75);
        List<InterestRateMatrixRequest> allRates = new ArrayList<>();
        
        allRates.addAll(Arrays.asList(
            InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(50000))
                .maxAmount(BigDecimal.valueOf(50000000))
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(36))
                .interestRate(BigDecimal.valueOf(7.0))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .build(),
            InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(50000))
                .maxAmount(BigDecimal.valueOf(50000000))
                .minTermMonths(BigDecimal.valueOf(36))
                .maxTermMonths(BigDecimal.valueOf(120))
                .interestRate(BigDecimal.valueOf(7.75))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .remarks("Higher rate for long-term deposits")
                .build()
        ));
        
        allRates.addAll(getStandardCategoryRates(baseRate));
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Cumulative Fixed Deposit")
                .productCode("FD-CUM-001")
                .productType(ProductType.CUMULATIVE_FD)
                .description("Interest compounded quarterly and paid at maturity for maximum returns")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(120))
                .minAmount(BigDecimal.valueOf(50000))
                .maxAmount(BigDecimal.valueOf(50000000))
                .minBalanceRequired(BigDecimal.valueOf(50000))
                .baseInterestRate(baseRate)
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(true)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(2)
                        .description("Single or joint account")
                        .build(),
                    ProductRoleRequest.builder()
                        .roleType(RoleType.CO_OWNER)
                        .mandatory(false)
                        .minCount(0)
                        .maxCount(1)
                        .description("Joint account holder")
                        .build()
                ))
                
                .interestRateMatrix(allRates)
                .build();
        
        productService.createProduct(request);
        log.info("Created: Cumulative Fixed Deposit with {} rate slabs", allRates.size());
    }

    private void createNonCumulativeFD() {
        log.info("Creating Non-Cumulative Fixed Deposit...");
        
        BigDecimal baseRate = BigDecimal.valueOf(6.5);
        List<InterestRateMatrixRequest> allRates = new ArrayList<>();
        
        allRates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(60))
                .interestRate(baseRate)
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .build());
        
        allRates.addAll(getStandardCategoryRates(baseRate));
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Non-Cumulative Fixed Deposit")
                .productCode("FD-NCUM-001")
                .productType(ProductType.NON_CUMULATIVE_FD)
                .description("Interest paid monthly for regular income. Ideal for retirees and pension seekers.")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(12))
                .maxTermMonths(BigDecimal.valueOf(60))
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(10000000))
                .minBalanceRequired(BigDecimal.valueOf(100000))
                .baseInterestRate(baseRate)
                .interestCalculationMethod("SIMPLE")
                .interestPayoutFrequency("MONTHLY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(false)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(false)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Primary account holder")
                        .build()
                ))
                
                .charges(Arrays.asList(
                    ProductChargeRequest.builder()
                        .chargeName("Monthly Payout Processing Fee")
                        .chargeType("FEE")
                        .description("Processing fee for monthly interest payout")
                        .fixedAmount(BigDecimal.valueOf(50))
                        .frequency(ChargeFrequency.MONTHLY)
                        .waivable(false)
                        .build()
                ))
                
                .interestRateMatrix(allRates)
                .build();
        
        productService.createProduct(request);
        log.info("Created: Non-Cumulative Fixed Deposit with {} rate slabs", allRates.size());
    }

    private void createFlexiFD() {
        log.info("Creating Flexi Fixed Deposit...");
        
        BigDecimal baseRate = BigDecimal.valueOf(6.0);
        List<InterestRateMatrixRequest> allRates = new ArrayList<>();
        
        allRates.add(InterestRateMatrixRequest.builder()
                .minAmount(BigDecimal.valueOf(25000))
                .maxAmount(BigDecimal.valueOf(5000000))
                .minTermMonths(BigDecimal.valueOf(6))
                .maxTermMonths(BigDecimal.valueOf(36))
                .interestRate(BigDecimal.valueOf(6.25))
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .build());
        
        allRates.addAll(getStandardCategoryRates(baseRate));
        
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Flexi Fixed Deposit")
                .productCode("FD-FLEXI-001")
                .productType(ProductType.FLEXI_FD)
                .description("Auto-sweep FD with liquidity and FD benefits. Best of both worlds - savings and fixed deposit.")
                .effectiveDate(LocalDate.of(2025, 1, 1))
                .bankBranchCode("HEAD")
                .currencyCode("INR")
                .status(ProductStatus.ACTIVE)
                
                .minTermMonths(BigDecimal.valueOf(6))
                .maxTermMonths(BigDecimal.valueOf(36))
                .minAmount(BigDecimal.valueOf(25000))
                .maxAmount(BigDecimal.valueOf(5000000))
                .minBalanceRequired(BigDecimal.valueOf(25000))
                .baseInterestRate(baseRate)
                .interestCalculationMethod("COMPOUND")
                .interestPayoutFrequency("ON_MATURITY")
                
                .prematureWithdrawalAllowed(true)
                .partialWithdrawalAllowed(true)
                .loanAgainstDepositAllowed(true)
                .autoRenewalAllowed(true)
                .nomineeAllowed(true)
                .jointAccountAllowed(true)
                
                .tdsRate(BigDecimal.valueOf(10.0))
                .tdsApplicable(true)
                
                .allowedRoles(Arrays.asList(
                    ProductRoleRequest.builder()
                        .roleType(RoleType.OWNER)
                        .mandatory(true)
                        .minCount(1)
                        .maxCount(1)
                        .description("Primary account holder")
                        .build()
                ))
                
                .charges(Arrays.asList(
                    ProductChargeRequest.builder()
                        .chargeName("Auto-Sweep Service Fee")
                        .chargeType("FEE")
                        .description("Annual fee for auto-sweep facility")
                        .fixedAmount(BigDecimal.valueOf(500))
                        .frequency(ChargeFrequency.ANNUALLY)
                        .waivable(true)
                        .build()
                ))
                
                .interestRateMatrix(allRates)
                .build();
        
        productService.createProduct(request);
        log.info("Created: Flexi Fixed Deposit with {} rate slabs", allRates.size());
    }
}