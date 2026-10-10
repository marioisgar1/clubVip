package com.prolog.clubvip.view;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/** Business page "appointments of the day" (APPOINTMENT_VIEW): all employees, with filters. */
@Named("appointmentDayMb")
@ViewScoped
public class AppointmentDayMb extends AbstractAppointmentListMb {

    private static final long serialVersionUID = 1L;

    @Override
    protected boolean employeeFilter() {
        return true;
    }
}
