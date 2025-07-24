package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

@Mapper
public interface IF_AdminDashboardDao {
    int adminSelectOrderCount();
    int adminSelectMemberCount();
    int adminSelectProductCount();
    long adminSelectTotalPaymentAmount(LocalDate startDate, LocalDate endDate);
    long adminSelectCancelAmount(LocalDate startDate, LocalDate endDate);
    long adminSelectSalesAmount(LocalDate startDate, LocalDate endDate);
    long adminSelectMargin(LocalDate startDate, LocalDate endDate);
}
