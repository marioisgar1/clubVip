package com.prolog.clubvip.view;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/** Employee page "my appointments" (MY_APPOINTMENTS): the backend restricts to the employee's agenda.
 * Opens with every appointment from today on so upcoming days are not hidden. */
@Named("myAppointmentsMb")
@ViewScoped
public class MyAppointmentsMb extends AbstractAppointmentListMb {

    private static final long serialVersionUID = 1L;

    @Override
    protected boolean employeeFilter() {
        return false;
    }

    @Override
    protected boolean upcomingByDefault() {
        return true;
    }
}
