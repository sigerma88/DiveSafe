package ca.mcgill.ecse.divesafe.controller;

import java.util.ArrayList;
import java.util.List;
import ca.mcgill.ecse.divesafe.application.DiveSafeApplication;
import ca.mcgill.ecse.divesafe.model.Assignment;
import ca.mcgill.ecse.divesafe.model.DiveSafe;
import ca.mcgill.ecse.divesafe.model.Equipment;
import ca.mcgill.ecse.divesafe.model.EquipmentBundle;
import ca.mcgill.ecse.divesafe.model.Guide;
import ca.mcgill.ecse.divesafe.model.Item;
import ca.mcgill.ecse.divesafe.model.Member;
import ca.mcgill.ecse.divesafe.model.Guide.AvailableStatus;
import ca.mcgill.ecse.divesafe.model.Member.MemberStatusRegistered;

public class AssignmentController {
  private static DiveSafe diveSafe = DiveSafeApplication.getDiveSafe();

  private AssignmentController() {
  }

  public static List<TOAssignment> getAssignments() {
    List<TOAssignment> assignments = new ArrayList<>();

    for (var assignment : diveSafe.getAssignments()) {
      var newTOAssignment = wrapAssignment(assignment);
      assignments.add(newTOAssignment);
    }

    return assignments;
  }

  /**
   * Method to initiate the assignment for the members by giving them a guide (if
   * they asked for one) and their trip's schedule
   * 
   * @author Siger Ma
   * @return
   */

  public static String initiateAssignment() {

    // get data
    List<Guide> currentGuides = diveSafe.getGuides();
    List<Member> currentMembers = diveSafe.getMembers();

    for (Guide guide : currentGuides) {
      for (Member member : currentMembers) {
        if (guide.getAvailableStatus() == AvailableStatus.Available && member.getMemberStatusRegistered() == MemberStatusRegistered.Unassigned) {
          
          // get data about member
          int numDaysRequest = member.getNumDays();
          boolean needGuide = member.getGuideRequired();

          // Assignment
          if (!needGuide) {
            member.assignNoGuide();
            new Assignment(1, numDaysRequest, member, diveSafe);
          }
          else {
            if (guide.bookGuide(numDaysRequest)) {
              member.assignYesGuide();
              Assignment assignment = new Assignment(guide.takenForPeriod() + 1, guide.takenForPeriod() + numDaysRequest, member, diveSafe);
              assignment.setGuide(guide);
            }
          }

        }
      }
    }

    return null;
  }

  public static String cancelTrip(String userEmail) {
    return null;
  }

  public static String finishTrip(String userEmail) {
    return null;
  }

  public static String startTripsForDay(int day) {
    return null;
  }

  public static String confirmPayment(String userEmail, String authorizationCode) {
    return null;
  }

  public static String toggleBan(String userEmail) {
    return null;
  }

  /**
   * Helper method used to wrap the information in an <code>Assignment</code>
   * instance in an
   * instance of <code>TOAssignment</code>.
   *
   * @author Harrison Wang Oct 19, 2021
   * @param assignment - The <code>Assignment</code> instance to transfer the
   *                   information from.
   * @return A <code>TOAssignment</code> instance containing the information in
   *         the
   *         <code>Assignment</code> parameter.
   */
  private static TOAssignment wrapAssignment(Assignment assignment) {
    var member = assignment.getMember();

    // Initialize values for all necessary parameters.
    String memberEmail = member.getEmail();
    String memberName = member.getName();
    String guideEmail = assignment.hasGuide() ? assignment.getGuide().getEmail() : "";
    String guideName = assignment.hasGuide() ? assignment.getGuide().getName() : "";
    String hotelName = assignment.hasHotel() ? assignment.getHotel().getName() : "";

    int numDays = member.getNumDays();
    int startDay = assignment.getStartDay();
    int endDay = assignment.getEndDay();
    int totalCostForGuide = assignment.hasGuide() ? numDays * diveSafe.getPriceOfGuidePerDay() : 0;
    /*
     * Calculate the totalCostForEquipment.
     *
     * Sum the costs of all booked items depending on if they are an Equipment or
     * EquipmentBundle
     * instance to get the equipmentCostPerDay for this assignment.
     *
     * Multiply equipmentCostPerDay by nrOfDays to get totalCostForEquipment.
     */
    int equipmentCostPerDay = 0;
    for (var bookedItem : member.getItemBookings()) {
      Item item = bookedItem.getItem();
      if (item instanceof Equipment equipment) {
        equipmentCostPerDay += equipment.getPricePerDay() * bookedItem.getQuantity();
      } else if (item instanceof EquipmentBundle bundle) {
        int bundleCost = 0;
        for (var bundledItem : bundle.getBundleItems()) {
          bundleCost += bundledItem.getEquipment().getPricePerDay() * bundledItem.getQuantity();
        }
        // Discount only applicable if assignment includes guide, so check for that
        // before applying discount
        if (assignment.hasGuide()) {
          bundleCost = (int) (bundleCost * ((100.0 - bundle.getDiscount()) / 100.0));
        }
        equipmentCostPerDay += bundleCost * bookedItem.getQuantity();
      }
    }
    int totalCostForEquipment = equipmentCostPerDay * numDays;

    return new TOAssignment(memberEmail, memberName, guideEmail, guideName, hotelName, startDay,
        endDay, totalCostForGuide, totalCostForEquipment);
  }

}
