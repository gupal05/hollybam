package com.hollybam.hollybam.services.admin;

import java.time.LocalDate;

public interface IF_AdminDashboardService {
    int adminSelectOrderCount();
    int adminSelectMemberCount();
    int adminSelectProductCount();
    long adminSelectTotalPaymentAmount(LocalDate startDate, LocalDate endDate);
    long adminSelectCancelAmount(LocalDate startDate, LocalDate endDate);
    long adminSelectSalesAmount(LocalDate startDate, LocalDate endDate);
    long adminSelectMargin(LocalDate startDate, LocalDate endDate);
}
