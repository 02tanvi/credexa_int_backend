package com.app.product.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.app.product.dto.CreateProductRequest;
import com.app.product.dto.InterestRateMatrixRequest;
import com.app.product.dto.InterestRateMatrixResponse;
import com.app.product.dto.ProductBalanceTypeResponse;
import com.app.product.dto.ProductChargeRequest;
import com.app.product.dto.ProductChargeResponse;
import com.app.product.dto.ProductResponse;
import com.app.product.dto.ProductRoleRequest;
import com.app.product.dto.ProductRoleResponse;
import com.app.product.dto.ProductSummaryResponse;
import com.app.product.dto.ProductTransactionTypeResponse;
import com.app.product.dto.UpdateProductRequest;
import com.app.product.entity.InterestRateMatrix;
import com.app.product.entity.Product;
import com.app.product.entity.ProductBalanceType;
import com.app.product.entity.ProductCharge;
import com.app.product.entity.ProductRole;
import com.app.product.entity.ProductTransactionType;
import com.app.product.enums.ProductStatus;

/**
 * Mapper utility for converting between entities and DTOs
 */
@Component
public class ProductMapper {

    // ============ Product Mappings ============
    
    public Product toEntity(CreateProductRequest request) {
        Product product = Product.builder()
                .productName(request.getProductName())
                .productCode(request.getProductCode())
                .productType(request.getProductType())
                .description(request.getDescription())
                .effectiveDate(request.getEffectiveDate())
                .endDate(request.getEndDate())
                .bankBranchCode(request.getBankBranchCode())
                .currencyCode(request.getCurrencyCode())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT)
                .minTermMonths(request.getMinTermMonths())
                .maxTermMonths(request.getMaxTermMonths())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .minBalanceRequired(request.getMinBalanceRequired())
                .baseInterestRate(request.getBaseInterestRate())
                .interestCalculationMethod(request.getInterestCalculationMethod())
                .interestPayoutFrequency(request.getInterestPayoutFrequency())
                .prematureWithdrawalAllowed(request.getPrematureWithdrawalAllowed())
                .partialWithdrawalAllowed(request.getPartialWithdrawalAllowed())
                .loanAgainstDepositAllowed(request.getLoanAgainstDepositAllowed())
                .autoRenewalAllowed(request.getAutoRenewalAllowed())
                .nomineeAllowed(request.getNomineeAllowed())
                .jointAccountAllowed(request.getJointAccountAllowed())
                .tdsRate(request.getTdsRate())
                .tdsApplicable(request.getTdsApplicable())
                .build();

        // Add roles
        if (request.getAllowedRoles() != null) {
            request.getAllowedRoles().forEach(roleReq -> 
                product.addRole(toRoleEntity(roleReq, product))
            );
        }

        // Add charges
        if (request.getCharges() != null) {
            request.getCharges().forEach(chargeReq -> 
                product.addCharge(toChargeEntity(chargeReq, product))
            );
        }

        // Add interest rate matrix
        if (request.getInterestRateMatrix() != null) {
            request.getInterestRateMatrix().forEach(rateReq -> 
                product.addInterestRateSlab(toInterestRateEntity(rateReq, product))
            );
        }

        return product;
    }

    public void updateEntity(Product product, UpdateProductRequest request) {
        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getEndDate() != null) {
            product.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getMinTermMonths() != null) {
            product.setMinTermMonths(BigDecimal.valueOf(request.getMinTermMonths()));
        }
        if (request.getMaxTermMonths() != null) {
            product.setMaxTermMonths(BigDecimal.valueOf(request.getMaxTermMonths()));
        }
        if (request.getMinAmount() != null) {
            product.setMinAmount(request.getMinAmount());
        }
        if (request.getMaxAmount() != null) {
            product.setMaxAmount(request.getMaxAmount());
        }
        if (request.getMinBalanceRequired() != null) {
            product.setMinBalanceRequired(request.getMinBalanceRequired());
        }
        if (request.getBaseInterestRate() != null) {
            product.setBaseInterestRate(request.getBaseInterestRate());
        }
        if (request.getInterestCalculationMethod() != null) {
            product.setInterestCalculationMethod(request.getInterestCalculationMethod());
        }
        if (request.getInterestPayoutFrequency() != null) {
            product.setInterestPayoutFrequency(request.getInterestPayoutFrequency());
        }
        if (request.getPrematureWithdrawalAllowed() != null) {
            product.setPrematureWithdrawalAllowed(request.getPrematureWithdrawalAllowed());
        }
        if (request.getPartialWithdrawalAllowed() != null) {
            product.setPartialWithdrawalAllowed(request.getPartialWithdrawalAllowed());
        }
        if (request.getLoanAgainstDepositAllowed() != null) {
            product.setLoanAgainstDepositAllowed(request.getLoanAgainstDepositAllowed());
        }
        if (request.getAutoRenewalAllowed() != null) {
            product.setAutoRenewalAllowed(request.getAutoRenewalAllowed());
        }
        if (request.getNomineeAllowed() != null) {
            product.setNomineeAllowed(request.getNomineeAllowed());
        }
        if (request.getJointAccountAllowed() != null) {
            product.setJointAccountAllowed(request.getJointAccountAllowed());
        }
        if (request.getTdsRate() != null) {
            product.setTdsRate(request.getTdsRate());
        }
        if (request.getTdsApplicable() != null) {
            product.setTdsApplicable(request.getTdsApplicable());
        }
        if (request.getUpdatedBy() != null) {
            product.setUpdatedBy(request.getUpdatedBy());
        }
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .productCode(product.getProductCode())
                .productType(product.getProductType())
                .description(product.getDescription())
                .effectiveDate(product.getEffectiveDate())
                .endDate(product.getEndDate())
                .bankBranchCode(product.getBankBranchCode())
                .currencyCode(product.getCurrencyCode())
                .status(product.getStatus())
                .minTermMonths(product.getMinTermMonths() != null ? product.getMinTermMonths().intValue() : null)
                .maxTermMonths(product.getMaxTermMonths() != null ? product.getMaxTermMonths().intValue() : null)
                .minAmount(product.getMinAmount())
                .maxAmount(product.getMaxAmount())
                .minBalanceRequired(product.getMinBalanceRequired())
                .baseInterestRate(product.getBaseInterestRate())
                .interestCalculationMethod(product.getInterestCalculationMethod())
                .interestPayoutFrequency(product.getInterestPayoutFrequency())
                .prematureWithdrawalAllowed(product.getPrematureWithdrawalAllowed())
                .partialWithdrawalAllowed(product.getPartialWithdrawalAllowed())
                .loanAgainstDepositAllowed(product.getLoanAgainstDepositAllowed())
                .autoRenewalAllowed(product.getAutoRenewalAllowed())
                .nomineeAllowed(product.getNomineeAllowed())
                .jointAccountAllowed(product.getJointAccountAllowed())
                .tdsRate(product.getTdsRate())
                .tdsApplicable(product.getTdsApplicable())
                .allowedRoles(toRoleResponseList(product.getAllowedRoles()))
                .charges(toChargeResponseList(product.getCharges()))
                .interestRateMatrix(toInterestRateResponseList(product.getInterestRateMatrix()))
                .transactionTypes(toTransactionTypeResponseList(product.getTransactionTypes()))
                .balanceTypes(toBalanceTypeResponseList(product.getBalanceTypes()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .currentlyActive(product.isCurrentlyActive())
                .build();
    }

    public ProductSummaryResponse toSummaryResponse(Product product) {
        return ProductSummaryResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .productCode(product.getProductCode())
                .productType(product.getProductType())
                .description(product.getDescription())
                .status(product.getStatus())
                .effectiveDate(product.getEffectiveDate())
                .endDate(product.getEndDate())
                .baseInterestRate(product.getBaseInterestRate())
                .minAmount(product.getMinAmount())
                .maxAmount(product.getMaxAmount())
                .minTermMonths(product.getMinTermMonths() != null ? product.getMinTermMonths().intValue() : null)
                .maxTermMonths(product.getMaxTermMonths() != null ? product.getMaxTermMonths().intValue() : null)
                .currentlyActive(product.isCurrentlyActive())
                .build();
    }

    // ============ ProductRole Mappings ============
    
    public ProductRole toRoleEntity(ProductRoleRequest request, Product product) {
        return ProductRole.builder()
                .product(product)
                .roleType(request.getRoleType())
                .mandatory(request.getMandatory())
                .minCount(request.getMinCount())
                .maxCount(request.getMaxCount())
                .description(request.getDescription())
                .build();
    }

    public ProductRoleResponse toRoleResponse(ProductRole role) {
        return ProductRoleResponse.builder()
                .id(role.getId())
                .roleType(role.getRoleType())
                .mandatory(role.getMandatory())
                .minCount(role.getMinCount())
                .maxCount(role.getMaxCount())
                .description(role.getDescription())
                .build();
    }

    public List<ProductRoleResponse> toRoleResponseList(List<ProductRole> roles) {
        if (roles == null) return new ArrayList<>();
        return roles.stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    // ============ ProductCharge Mappings ============
    
    public ProductCharge toChargeEntity(ProductChargeRequest request, Product product) {
        return ProductCharge.builder()
                .product(product)
                .chargeName(request.getChargeName())
                .chargeType(request.getChargeType())
                .description(request.getDescription())
                .fixedAmount(request.getFixedAmount())
                .percentageRate(request.getPercentageRate())
                .frequency(request.getFrequency())
                .applicableTransactionTypes(request.getApplicableTransactionTypes())
                .waivable(request.getWaivable())
                .minCharge(request.getMinCharge())
                .maxCharge(request.getMaxCharge())
                .active(true)
                .build();
    }

    public ProductChargeResponse toChargeResponse(ProductCharge charge) {
        return ProductChargeResponse.builder()
                .id(charge.getId())
                .chargeName(charge.getChargeName())
                .chargeType(charge.getChargeType())
                .description(charge.getDescription())
                .fixedAmount(charge.getFixedAmount())
                .percentageRate(charge.getPercentageRate())
                .frequency(charge.getFrequency())
                .applicableTransactionTypes(charge.getApplicableTransactionTypes())
                .waivable(charge.getWaivable())
                .minCharge(charge.getMinCharge())
                .maxCharge(charge.getMaxCharge())
                .active(charge.getActive())
                .build();
    }

    public List<ProductChargeResponse> toChargeResponseList(List<ProductCharge> charges) {
        if (charges == null) return new ArrayList<>();
        return charges.stream()
                .map(this::toChargeResponse)
                .collect(Collectors.toList());
    }

    // ============ InterestRateMatrix Mappings ============
    
    public InterestRateMatrix toInterestRateEntity(InterestRateMatrixRequest request, Product product) {
        return InterestRateMatrix.builder()
                .product(product)
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .minTermMonths(request.getMinTermMonths())
                .maxTermMonths(request.getMaxTermMonths())
                .customerClassification(request.getCustomerClassification())
                .interestRate(request.getInterestRate())
                .additionalRate(request.getAdditionalRate())
                .effectiveDate(request.getEffectiveDate())
                .endDate(request.getEndDate())
                .remarks(request.getRemarks())
                .build();
    }

    public InterestRateMatrixResponse toInterestRateResponse(InterestRateMatrix rate) {
        return InterestRateMatrixResponse.builder()
                .id(rate.getId())
                .minAmount(rate.getMinAmount())
                .maxAmount(rate.getMaxAmount())
                .minTermMonths(rate.getMinTermMonths() != null ? rate.getMinTermMonths().intValue() : null)
                .maxTermMonths(rate.getMaxTermMonths() != null ? rate.getMaxTermMonths().intValue() : null)
                .customerClassification(rate.getCustomerClassification())
                .interestRate(rate.getInterestRate())
                .additionalRate(rate.getAdditionalRate())
                .effectiveDate(rate.getEffectiveDate())
                .endDate(rate.getEndDate())
                .remarks(rate.getRemarks())
                .totalRate(rate.getTotalRate())
                .build();
    }

    public List<InterestRateMatrixResponse> toInterestRateResponseList(List<InterestRateMatrix> rates) {
        if (rates == null) return new ArrayList<>();
        return rates.stream()
                .map(this::toInterestRateResponse)
                .collect(Collectors.toList());
    }

    // ============ ProductTransactionType Mappings ============
    
    public ProductTransactionTypeResponse toTransactionTypeResponse(ProductTransactionType txnType) {
        return ProductTransactionTypeResponse.builder()
                .id(txnType.getId())
                .transactionType(txnType.getTransactionType())
                .allowed(txnType.getAllowed())
                .requiresApproval(txnType.getRequiresApproval())
                .description(txnType.getDescription())
                .validationRules(txnType.getValidationRules())
                .build();
    }

    public List<ProductTransactionTypeResponse> toTransactionTypeResponseList(List<ProductTransactionType> txnTypes) {
        if (txnTypes == null) return new ArrayList<>();
        return txnTypes.stream()
                .map(this::toTransactionTypeResponse)
                .collect(Collectors.toList());
    }

    // ============ ProductBalanceType Mappings ============
    
    public ProductBalanceTypeResponse toBalanceTypeResponse(ProductBalanceType balanceType) {
        return ProductBalanceTypeResponse.builder()
                .id(balanceType.getId())
                .balanceType(balanceType.getBalanceType())
                .tracked(balanceType.getTracked())
                .description(balanceType.getDescription())
                .build();
    }

    public List<ProductBalanceTypeResponse> toBalanceTypeResponseList(List<ProductBalanceType> balanceTypes) {
        if (balanceTypes == null) return new ArrayList<>();
        return balanceTypes.stream()
                .map(this::toBalanceTypeResponse)
                .collect(Collectors.toList());
    }
}
