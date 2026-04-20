package model;

import java.time.LocalDateTime;

/**
 * Holds the handover/return details carried on a BorrowRequest.
 * Sprint 3 — US-1.
 */
public class HandoverDetails {
    public enum Method { MEETUP, COURIER }
    public enum VehicleType { BIKE, CAR, VAN }

    private Method method;
    private String otp;               // 4-digit, shown to borrower, entered by lender
    private String returnOtp;         // 4-digit for the return direction
    // Meetup
    private String meetupLocation;
    private LocalDateTime meetupTime;
    // Courier (outbound)
    private String courierService;    // inDrive / Yango / Other / custom
    private String courierPerson;
    private String vehiclePlate;
    private String vehicleType;
    private String proofImageUrl;
    // Courier (return)
    private String returnCourierService;
    private String returnCourierPerson;
    private String returnVehiclePlate;
    private String returnVehicleType;
    private String returnProofImageUrl;

    public HandoverDetails() {}

    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getReturnOtp() { return returnOtp; }
    public void setReturnOtp(String returnOtp) { this.returnOtp = returnOtp; }

    public String getMeetupLocation() { return meetupLocation; }
    public void setMeetupLocation(String meetupLocation) { this.meetupLocation = meetupLocation; }

    public LocalDateTime getMeetupTime() { return meetupTime; }
    public void setMeetupTime(LocalDateTime meetupTime) { this.meetupTime = meetupTime; }

    public String getCourierService() { return courierService; }
    public void setCourierService(String courierService) { this.courierService = courierService; }

    public String getCourierPerson() { return courierPerson; }
    public void setCourierPerson(String courierPerson) { this.courierPerson = courierPerson; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getProofImageUrl() { return proofImageUrl; }
    public void setProofImageUrl(String proofImageUrl) { this.proofImageUrl = proofImageUrl; }

    public String getReturnCourierService() { return returnCourierService; }
    public void setReturnCourierService(String s) { this.returnCourierService = s; }

    public String getReturnCourierPerson() { return returnCourierPerson; }
    public void setReturnCourierPerson(String s) { this.returnCourierPerson = s; }

    public String getReturnVehiclePlate() { return returnVehiclePlate; }
    public void setReturnVehiclePlate(String s) { this.returnVehiclePlate = s; }

    public String getReturnVehicleType() { return returnVehicleType; }
    public void setReturnVehicleType(String s) { this.returnVehicleType = s; }

    public String getReturnProofImageUrl() { return returnProofImageUrl; }
    public void setReturnProofImageUrl(String s) { this.returnProofImageUrl = s; }
}
