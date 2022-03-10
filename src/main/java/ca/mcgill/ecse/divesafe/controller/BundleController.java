package ca.mcgill.ecse.divesafe.controller;

import java.util.*;

import ca.mcgill.ecse.divesafe.application.DiveSafeApplication;
import ca.mcgill.ecse.divesafe.model.DiveSafe;
import ca.mcgill.ecse.divesafe.model.Equipment;
import ca.mcgill.ecse.divesafe.model.EquipmentBundle;
import ca.mcgill.ecse.divesafe.model.Item;

public class BundleController {

  // Instance variables
  private static DiveSafe divesafe;

  /**
   * 
   * @author Jiahao Zhao, Siger Ma, Kevin Luo
   * @param name                - Name of the bundle
   * @param discount            - Discount applied on the bundle
   * @param equipmentNames      - Name of the equipments in the bundle
   * @param equipmentQuantities - Quantities of item in the bundle
   * @return String - Nothing if no errors or error message if there is one
   */

  public static String addEquipmentBundle(String name, int discount, List<String> equipmentNames,
      List<Integer> equipmentQuantities) {

    // Variables
    divesafe = DiveSafeApplication.getDiveSafe();
    String error = "";

    // Might be better implementation(?) 03/09 10pm
    // Constraints JZ and KL
    if (equipmentNames.size() <= 1) {
      error = "Equipment bundle must contain at least two distinct types of equipment";
      return error;
    } else {
      String firstEquipmentName = equipmentNames.get(0);
      for (int i = 1; i < equipmentNames.size(); i++) {
        if (!(equipmentNames.get(i).equals(firstEquipmentName))) {
          error = "";
          break;
        } else {
          error = "Equipment bundle must contain at least two distinct types of equipment";
        }
      }
    }

    // Constraints JZ
    if (discount < 0) {
      error = "Discount must be at least 0";
    }

    // Constraints JZ
    if (discount > 100) {
      error = "Discount must be no more than 100";
    }

    // Constraints SM and KL
    if (equipmentQuantities.size() <= 0) {
      error = "Each bundle item must have quantity greater than or equal to 1";
    } else {
      for (Integer quantity : equipmentQuantities) {
        if (quantity < 1) {
          error = "Each bundle item must have quantity greater than or equal to 1";
          break;
        }
      }
    }

    // Constraints KL
    if (name.isBlank() || name == null) {
      error = "Equipment bundle name cannot be empty";
    }

    // Constraints KL
    for (String equipment : equipmentNames) {
      if (!(Item.hasWithName(equipment))) {
        error = String.format("Equipment %s does not exist", equipment);
        break;
      }
    }

    // Constraint to check for duplicates EJ
    List<EquipmentBundle> equipmentBundles = divesafe.getBundles();
    for (EquipmentBundle bundle : equipmentBundles) {
      if (name.equals(bundle.getName())) {
        error = String.format("A bookable item called %s already exists", name);
        break;
      }
    }

    // If error return KL
    if (!error.isEmpty()) {
      return error;
    }



    // Try-catch KL
    try {
      // Create bundle by SM
      EquipmentBundle aBundle = divesafe.addBundle(name, discount);

      // Add bundle items by SM
      for (int i = 0; i < equipmentNames.size(); i++) {

        Equipment aEquipment = (Equipment) Item.getWithName(equipmentNames.get(i));
        int aQuantity = equipmentQuantities.get(i);

        divesafe.addBundleItem(aQuantity, aBundle, aEquipment);
      }

      return error;

    } catch (Exception e) {
      return e.getMessage();
    }

  }

  public static String updateEquipmentBundle(String oldName, String newName, int newDiscount,
      List<String> newEquipmentNames, List<Integer> newEquipmentQuantities) {
    return null;
  }

  public static void deleteEquipmentBundle(String name) {
  }
}
