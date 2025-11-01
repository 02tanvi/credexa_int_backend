# Email Templates Guide

## Overview

All email templates have **pre-written standard content**. You only need to provide customer-specific data like name, account number, etc.

The templates are designed to look like professional business emails, not promotional websites.

---

## Available Templates

### 1. Welcome Template (`welcome.html`)

**When to use**: When a new user signs up

**Pre-written content**:

- "Welcome to Credexa!"
- "Thank you for joining Credexa. We're excited to have you on board."
- "Your account has been successfully created..."

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("name", "John Doe");
templateData.put("loginUrl", "https://credexa.com/login"); // Optional
```

**Example**:

```java
emailServiceClient.sendTemplatedEmail(
    "customer@example.com",
    "Welcome to Credexa!",
    "welcome",
    templateData
);
```

---

### 2. KYC Approval Notification (`account-notification.html`)

**When to use**: When KYC verification is approved

**Pre-written content**:

- "KYC Verification Approved"
- "We are pleased to inform you that your KYC verification has been successfully approved."
- "You can now enjoy full access to Credexa banking services."

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("name", "John Doe");
```

**Example**:

```java
emailServiceClient.sendTemplatedEmail(
    "customer@example.com",
    "KYC Verification Approved",
    "account-notification",
    templateData
);
```

### 3. New Customer Registration (`new-customer.html`)

**When to use**: When a new customer completes registration

**Pre-written content**:

- "🎉 Congratulations!"
- "We are thrilled to have you as a new customer at Credexa."
- Shows customer details with labels
- Lists features: "Open Fixed Deposit accounts", "24/7 access", etc.
- "Thank you for choosing Credexa..."

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", "John Doe");
templateData.put("customerId", "CUST123456");
templateData.put("email", "john@example.com");
templateData.put("mobile", "+91 9876543210");
templateData.put("registrationDate", "October 21, 2025");
templateData.put("loginUrl", "https://credexa.com/login"); // Optional
```

**Example**:

```java
emailServiceClient.sendTemplatedEmail(
    customer.getEmail(),
    "Welcome to Credexa!",
    "new-customer",
    Map.of(
        "customerName", customer.getFullName(),
        "customerId", customer.getId().toString(),
        "email", customer.getEmail(),
        "mobile", customer.getMobileNumber(),
        "registrationDate", LocalDate.now().toString(),
        "loginUrl", "https://credexa.com/login"
    )
);
```

### 4. New Account Opening (`new-account.html`)

**When to use**: When a new FD/Savings account is opened

**Pre-written content**:

- "🎊 Great News!"
- "Your new Fixed Deposit account has been successfully opened with Credexa."
- Shows account details with labels
- Displays deposit amount highlighted
- Shows maturity date and amount
- Lists benefits: "Secure and guaranteed returns", "24/7 access", etc.
- "Thank you for continuing your financial journey..."

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", "John Doe");
templateData.put("accountNumber", "FD123456789");
templateData.put("accountType", "Fixed Deposit");
templateData.put("openingDate", "October 21, 2025");

// Optional but recommended for FD accounts
templateData.put("depositAmount", "50,000");
templateData.put("interestRate", "7.5");
templateData.put("maturityDate", "October 21, 2026");
templateData.put("maturityAmount", "53,750");
templateData.put("accountUrl", "https://credexa.com/accounts/FD123456789"); // Optional
```

**Example**:

```java
emailServiceClient.sendTemplatedEmail(
    fdAccount.getCustomerEmail(),
    "New Account Opened Successfully!",
    "new-account",
    Map.of(
        "customerName", fdAccount.getCustomerName(),
        "accountNumber", fdAccount.getAccountNumber(),
        "accountType", "Fixed Deposit",
        "openingDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
        "depositAmount", String.format("%,.0f", fdAccount.getDepositAmount()),
        "interestRate", fdAccount.getInterestRate().toString(),
        "maturityDate", fdAccount.getMaturityDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
        "maturityAmount", String.format("%,.0f", fdAccount.getMaturityAmount()),
        "accountUrl", "https://credexa.com/accounts/" + fdAccount.getAccountNumber()
    )
);
```

---

## 🎨 Logo Configuration

**Logo Path**: All templates reference: `../static/images/logo.png`

**Add Your Logo**:

1. Place PNG file at: `email-service/src/main/resources/static/images/logo.png`
2. Recommended size: 50px width (auto-height)
3. Format: PNG with transparent background

---

## 📋 Complete Usage Examples

### CustomerService Integration

```java
// In CustomerService.java after creating customer
emailServiceClient.sendTemplatedEmail(
    customer.getEmail(),
    "Welcome to Credexa!",
    "new-customer",
    Map.of(
        "customerName", customer.getFullName(),
        "customerId", customer.getId().toString(),
        "email", customer.getEmail(),
        "mobile", customer.getMobileNumber(),
        "registrationDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
        "loginUrl", "https://credexa.com/login"
    )
);
```

### FD Account Service Integration

```java
// In FDAccountService.java after creating FD account
emailServiceClient.sendTemplatedEmail(
    fdAccount.getCustomerEmail(),
    "New Account Opened Successfully!",
    "new-account",
    Map.of(
        "customerName", fdAccount.getCustomerName(),
        "accountNumber", fdAccount.getAccountNumber(),
        "accountType", "Fixed Deposit",
        "openingDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
        "depositAmount", String.format("%,.0f", fdAccount.getDepositAmount()),
        "interestRate", fdAccount.getInterestRate().toString(),
        "maturityDate", fdAccount.getMaturityDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
        "maturityAmount", String.format("%,.0f", fdAccount.getMaturityAmount()),
        "accountUrl", "https://credexa.com/accounts/" + fdAccount.getAccountNumber()
    )
);
```

---

## Key Points

✅ **Templates have standard text** - Only provide customer data  
✅ **Simple API** - Just pass name, account number, etc.  
✅ **Professional design** - Looks like real business emails  
✅ **No support links** - Templates don't include help/support sections
