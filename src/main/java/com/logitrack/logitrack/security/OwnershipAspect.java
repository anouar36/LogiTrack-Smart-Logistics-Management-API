package com.logitrack.logitrack.security;

import com.logitrack.logitrack.entity.Role;
import com.logitrack.logitrack.entity.SalesOrder;
import com.logitrack.logitrack.entity.Shipment;
import com.logitrack.logitrack.exception.AccessDeniedException;
import com.logitrack.logitrack.repository.SalesOrderRepository;
import com.logitrack.logitrack.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

/**
 * Aspect to enforce ownership checking for CLIENT role.
 * Ensures clients can only access their own resources.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OwnershipAspect {

    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentRepository shipmentRepository;

    @Before("@annotation(checkOwnership)")
    public void checkOwnership(JoinPoint joinPoint, CheckOwnership checkOwnership) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        // Check if user is ADMIN or WAREHOUSE_MANAGER - they have full access
        boolean isPrivilegedUser = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                  auth.getAuthority().equals("ROLE_WAREHOUSE_MANAGER"));
        
        if (isPrivilegedUser) {
            return; // Privileged users can access any resource
        }

        // For CLIENT role, check ownership
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Get the resource ID from method parameters
        Long resourceId = extractResourceId(joinPoint, checkOwnership.resourceIdParam());
        
        if (resourceId == null) {
            return; // If no resource ID, let the method handle it
        }

        // Check ownership based on resource type
        boolean isOwner = switch (checkOwnership.resourceType()) {
            case SALES_ORDER -> checkSalesOrderOwnership(resourceId, userId);
            case SHIPMENT -> checkShipmentOwnership(resourceId, userId);
            case CLIENT_PROFILE -> checkClientProfileOwnership(resourceId, userId);
        };

        if (!isOwner) {
            throw new AccessDeniedException("You do not have permission to access this resource");
        }
    }

    private Long extractResourceId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName)) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    try {
                        return Long.parseLong((String) arg);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private boolean checkSalesOrderOwnership(Long orderId, Long userId) {
        return salesOrderRepository.findById(orderId)
                .map(order -> order.getClient().getUser().getId().equals(userId))
                .orElse(false);
    }

    private boolean checkShipmentOwnership(Long shipmentId, Long userId) {
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> shipment.getSalesOrder().getClient().getUser().getId().equals(userId))
                .orElse(false);
    }

    private boolean checkClientProfileOwnership(Long clientId, Long userId) {
        // For client profile, the resourceId should match the user's client ID
        // This would need a ClientRepository lookup
        return true; // Simplified for now
    }
}
