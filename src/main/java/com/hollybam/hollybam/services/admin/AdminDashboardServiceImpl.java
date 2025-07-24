package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminDashboardDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class AdminDashboardServiceImpl implements IF_AdminDashboardService {
    @Autowired
    private IF_AdminDashboardDao adminDashboardDao;

    @Override
    @Transactional
    public int adminSelectOrderCount(){
        return adminDashboardDao.adminSelectOrderCount();
    }

    @Override
    @Transactional
    public int adminSelectMemberCount(){
        return adminDashboardDao.adminSelectMemberCount();
    }

    @Override
    @Transactional
    public int adminSelectProductCount(){
        return adminDashboardDao.adminSelectProductCount();
    }

    @Override
    @Transactional
    public long adminSelectTotalPaymentAmount(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectTotalPaymentAmount(startDate, endDate);
    }

    @Override
    @Transactional
    public long adminSelectCancelAmount(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectCancelAmount(startDate, endDate);
    }

    @Override
    @Transactional
    public long adminSelectSalesAmount(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectSalesAmount(startDate, endDate);
    }

    @Override
    @Transactional
    public long adminSelectMargin(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectMargin(startDate, endDate);
    }
}
