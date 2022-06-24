// package com.mora.javaservice;

// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.text.DateFormat;
// import java.text.SimpleDateFormat;
// import java.time.chrono.HijrahDate;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Calendar;
// import java.util.Collections;
// import java.util.Date;
// import java.util.List;
// import java.util.Locale;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import org.apache.logging.log4j.core.parser.ParseException;

// import com.konylabs.middleware.common.JavaService2;
// import com.konylabs.middleware.controller.DataControllerRequest;
// import com.konylabs.middleware.controller.DataControllerResponse;
// import com.konylabs.middleware.dataobject.Result;
// import com.mora.dto.BouncedChecks;
// import com.mora.dto.CIDetail;
// import com.mora.dto.CitizenInfo;
// import com.mora.dto.Default;
// import com.mora.dto.Employer;
// import com.mora.dto.GovernmentSector;
// import com.mora.dto.Judgment;
// import com.mora.dto.PrivateSector;
// import com.mora.dto.Table;
// public class LoanSimulation implements JavaService2 {
// 	private static final Logger logger = LogManager.getLogger(LoanSimulation.class);

// 	@Override
// 	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
// 			DataControllerResponse response) throws Exception {
		
		
// 		Result result = new Result();


// 		return result;
// 	}

// 	public void run(String... args) throws Exception {
//        final CitizenInfo citizenInfo = new CitizenInfo();
//        citizenInfo.setDateOfBirthG("15-06-1996");
//        citizenInfo.setDateOfBirthH("25-11-1404");

//        List<CIDetail> ciDetail = new ArrayList<>();
//        ciDetail.add(new CIDetail("UTIL", "A", "", "M", "800.00", ""));
//        ciDetail.add(new CIDetail("UTIL", "U", "", "M", "400", ""));
//        ciDetail.add(new CIDetail("UTIL", "A", "IJRH", "Q", "400", ""));

//        List<Table> tawarruqTableList = new ArrayList<>();
//        tawarruqTableList.add(new Table("Tawarruq", 0, 2999, 45, 25));
//        tawarruqTableList.add(new Table("Tawarruq", 3000, 14999, 45, 30));
//        tawarruqTableList.add(new Table("Tawarruq", 15000, 999999, 45, 33));

//        List<Table> murabahaTableList = new ArrayList<>();
//        murabahaTableList.add(new Table("Murabaha", 0, 2999, 45, 33));
//        murabahaTableList.add(new Table("Murabaha", 3000, 16999, 45, 33));
//        murabahaTableList.add(new Table("Murabaha", 17000, 999999, 55, 40));


//        //Call SIMAH Salary Certificate for Private employment (with EmployerTypeID = 3)
//        //If response is received AND ["employmentStatus"<> "نشيط" OR  "Active"]
//        PrivateSector privateSector = new PrivateSector();
//        privateSector.setEmploymentStatus(EMPLOYEE_ACTIVE_STATUS_AR);
//        privateSector.setBasicWage("10000");
//        privateSector.setHousingAllowance("2500");
//        privateSector.setOtherAllowance("500");
//        privateSector.setDateOfJoining("01/01/2020");
//        if (privateSector.getEmploymentStatus().equals(EMPLOYEE_ACTIVE_STATUS_EN) ||
//                privateSector.getEmploymentStatus().equals(EMPLOYEE_ACTIVE_STATUS_AR)) {


//            String age = getAge(citizenInfo, false);

//            String monthlyNetSalary = getMNS(privateSector);
//            String lengthOfService = getLOS(privateSector);

//            String globalDebtServicing = getGlobalDebtServicing(ciDetail);
//            String maxOverallDTI = getMaxDTI(monthlyNetSalary, globalDebtServicing, tawarruqTableList);

//            String ijarahlDebtServicing = getIjarahDebtServicing(ciDetail);
//            String maxInternalDTI = getInternalDTI(monthlyNetSalary, ijarahlDebtServicing, tawarruqTableList);

//            getOfferLoanAmount(monthlyNetSalary, globalDebtServicing, ijarahlDebtServicing, "36", "25", murabahaTableList);
//        } else {
//            //Call SIMAH Salary Certificate for Govt employment (with EmployerTypeID = 1)


//            /*
//             * Repeat steps same as Private Sector Employee
//             * */

//        }
//    }
// 	public String getAge(CitizenInfo citizenInfo, boolean isSaudiCitizen) throws ParseException {
//         if (isSaudiCitizen) {
//             final DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//             final String dateString = citizenInfo.getDateOfBirthH();
//             final Date dateFrom = formatter.parse(dateString);
//             final String dateToString = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(HijrahDate.now());
//             final Date dateTo = formatter.parse(dateToString);
//             return getYears(dateFrom, dateTo);
//         } else {
//             final DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//             final String dateString = citizenInfo.getDateOfBirthG();
//             final Date dateFrom = formatter.parse(dateString);
//             final Date dateTo = new Date();
//             return getYears(dateFrom, dateTo);
//         }
//     }

// 	public String getYears(final Date dateFrom, final Date dateTo) {
//         final DateFormat df = new SimpleDateFormat("yyyy", Locale.US);
//         final Calendar calendar = Calendar.getInstance();
//         calendar.setTime(dateFrom);

//         final List<String> months = new ArrayList<>();
//         while (calendar.getTime().getTime() <= dateTo.getTime()) {
//             months.add(df.format(calendar.getTime()));
//             calendar.add(Calendar.YEAR, 1);
//         }
//         return String.valueOf(months.size() - 1);
//     }
//     /**/


//     //Returning 1 if employee is pensioner : Correct
//     //Q:- employee status nai hai or 1 se ziada employer hain
//     public String getPensioner(List<Employer> employerList) {
//         for (Employer employer : employerList) {
//             if (employer.getENMA().equals(EMPLOYEE_PENSIONER1) ||
//                     employer.getENMA().equals(EMPLOYEE_PENSIONER2)) {
//                 return "1";
//             }
//         }
//         return "0";
//     }


//     /*
//      * Monthly Net Salary
//      * */
//     public String getMNS(PrivateSector privateSector) {
//         final BigDecimal deductPercentage = new BigDecimal("0.1");
//         final BigDecimal basicWage = new BigDecimal(privateSector.getBasicWage());
//         final BigDecimal housingAllowance = new BigDecimal(privateSector.getHousingAllowance());
//         final BigDecimal otherAllowance = new BigDecimal(privateSector.getOtherAllowance());

//         final BigDecimal basinIncomeHouseAllowance = basicWage.add(housingAllowance);
//         final BigDecimal mnp = basinIncomeHouseAllowance.subtract(deductPercentage.multiply(basinIncomeHouseAllowance)).add(otherAllowance);
//         return String.valueOf(mnp.intValue());
//     }


//     //Q:- totalAllowance taken as OtherAllowance
//     //Q:- housingAllowance is not available so taken as zero
//     //Remarks: It is not satisfied
//     public String getMNS(final GovernmentSector governmentSector) {
//         final BigDecimal deductPercentage = new BigDecimal("0.09");
//         final BigDecimal basicWage = new BigDecimal(governmentSector.getPayslipInfo().getBasicSalary());
//         final BigDecimal housingAllowance = new BigDecimal("0");
//         final BigDecimal otherAllowance = new BigDecimal(governmentSector.getPayslipInfo().getTotalAllownces());

//         final BigDecimal basinIncomeHouseAllowance = basicWage.add(housingAllowance);
//         final BigDecimal mnp = basinIncomeHouseAllowance.subtract(deductPercentage.multiply(basinIncomeHouseAllowance)).add(otherAllowance);
//         return String.valueOf(mnp.intValue());
//     }
//     /**/


//     /*
//      * Length of Service
//      * */
//     //Q:- Taken date as 01/01/2020 - dd/MM/yyyy :-satisfied
//     public String getLOS(final PrivateSector privateSector) throws ParseException {
//         final String dateString = privateSector.getDateOfJoining();
//         final DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//         final Date dateFrom = formatter.parse(dateString);
//         final Date dateTo = new Date();
//         return getMonths(dateFrom, dateTo);
//     }

//     //Q:- Taken date as 2018-10-10 - yyyy-MM-dd :-satisfied
//     public String getLOS(GovernmentSector governmentSector) throws ParseException {
//         final String dateString = governmentSector.getEmploymentInfo().getAgencyEmploymentDate();
//         final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//         final Date dateFrom = formatter.parse(dateString);
//         final Date dateTo = new Date();
//         return getMonths(dateFrom, dateTo);
//     }

//     public String getMonths(final Date dateFrom, final Date dateTo) {
//         final DateFormat df = new SimpleDateFormat("MMMM", Locale.US);
//         final Calendar calendar = Calendar.getInstance();
//         calendar.setTime(dateFrom);

//         final List<String> months = new ArrayList<>();
//         while (calendar.getTime().getTime() <= dateTo.getTime()) {
//             months.add(df.format(calendar.getTime()));
//             calendar.add(Calendar.MONTH, 1);
//         }
//         return String.valueOf(months.size() - 1);
//     }
//     /**/


//     /*
//      * Ijarah Debt Servicing
//      * Global Debt Servicing
//      * MaxOverallDTI
//      * MaxInternalDTI
//      * */
//     public String getGlobalDebtServicing(final List<CIDetail> ciDetailList) {
//         BigDecimal globalDebtServicing = BigDecimal.ZERO;
//         if (ciDetailList != null)
//             for (CIDetail detail : ciDetailList) {
//                 if (NON_FINANCIAL_PRODUCTS.contains(detail.getCI_PRD())) {
//                     continue;
//                 }
//                 if (!detail.getCI_STATUS().equals("A")) {
//                     continue;
//                 }

//                 String frequency = detail.getCI_FRQ();
//                 switch (frequency) {
//                     case "M":
//                         globalDebtServicing = globalDebtServicing.add(new BigDecimal(detail.getCI_INSTL()));
//                         break;
//                     case "Q": {
//                         BigDecimal instl = new BigDecimal(detail.getCI_INSTL()).divide(BigDecimal.valueOf(4), 0, RoundingMode.HALF_UP);
//                         globalDebtServicing = globalDebtServicing.add(instl);
//                         break;
//                     }
//                     case "S": {
//                         BigDecimal instl = new BigDecimal(detail.getCI_INSTL()).divide(BigDecimal.valueOf(6), 0, RoundingMode.HALF_UP);
//                         globalDebtServicing = globalDebtServicing.add(instl);
//                         break;
//                     }
//                 }

//             }
//         return String.valueOf(globalDebtServicing);
//     }


//     public String getIjarahDebtServicing(final List<CIDetail> ciDetailList) {
//         BigDecimal ijarahDebtServicing = BigDecimal.ZERO;
//         if (ciDetailList != null)
//             for (CIDetail detail : ciDetailList) {
//                 if (NON_FINANCIAL_PRODUCTS.contains(detail.getCI_PRD())) {
//                     continue;
//                 }
//                 if (!detail.getCI_STATUS().equals("A")) {
//                     continue;
//                 }
//                 if (!detail.getCI_CRDTR().equals("IJRH")) {
//                     continue;
//                 }

//                 String frequency = detail.getCI_FRQ();
//                 switch (frequency) {
//                     case "M":
//                         ijarahDebtServicing = ijarahDebtServicing.add(new BigDecimal(detail.getCI_INSTL()));
//                         break;
//                     case "Q": {
//                         BigDecimal instl = new BigDecimal(detail.getCI_INSTL()).divide(BigDecimal.valueOf(4), 0, RoundingMode.HALF_UP);
//                         ijarahDebtServicing = ijarahDebtServicing.add(instl);
//                         break;
//                     }
//                     case "S": {
//                         BigDecimal instl = new BigDecimal(detail.getCI_INSTL()).divide(BigDecimal.valueOf(6), 0, RoundingMode.HALF_UP);
//                         ijarahDebtServicing = ijarahDebtServicing.add(instl);
//                         break;
//                     }
//                 }

//             }
//         return String.valueOf(ijarahDebtServicing);
//     }


//     public String getMaxDTI(final String monthlyNetSalary, final String globalDebtServicingString, final List<Table> tawarruqTableList) {
//         logger.error("calculation overallDTI");
//         final BigDecimal globalDebtServicing = new BigDecimal(globalDebtServicingString);
//         logger.error("ijarahDebitServing: [{}]", globalDebtServicing);
//         final BigDecimal salary = new BigDecimal(monthlyNetSalary);
//         logger.error("salary: [{}]", salary);
//         final BigDecimal dti = globalDebtServicing.divide(salary, 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
//         logger.error("dti: [{}]", dti);
//         int maxOverallDTI = 0;
//         for (Table table : tawarruqTableList) {
//             if (salary.intValue() > table.getSalaryRangeMin() && salary.intValue() < table.getSalaryRangeMax()) {
//                 maxOverallDTI = table.getMaxOverallDTI();
//                 logger.error("maxOverallDTI: [{}]", maxOverallDTI);
//             }
//         }
//         if (dti.intValue() >= maxOverallDTI) {
//             return "0";
//         } else {
//             return "1";
//         }
//     }


//     public String getInternalDTI(final String monthlyNetSalary, final String ijarahDebtServicingString, final List<Table> tawarruqTableList) {
//         logger.error("calculation internalDTI");
//         final BigDecimal ijarahDebtServicing = new BigDecimal(ijarahDebtServicingString);
//         logger.error("ijarahDebitServing: [{}]", ijarahDebtServicing);
//         final BigDecimal salary = new BigDecimal(monthlyNetSalary);
//         logger.error("salary: [{}]", salary);
//         final BigDecimal dti = ijarahDebtServicing.divide(salary, 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
//         logger.error("dti: [{}]", dti);
//         int maxInternalDTI = 0;
//         for (Table table : tawarruqTableList) {
//             if (salary.intValue() > table.getSalaryRangeMin() && salary.intValue() < table.getSalaryRangeMax()) {
//                 maxInternalDTI = table.getMaxInternalDTI();
//                 logger.error("maxInternalDTI: [{}]", maxInternalDTI);
//             }
//         }
//         if (dti.intValue() >= maxInternalDTI) {
//             return "0";
//         } else {
//             return "1";
//         }
//     }
//     /**/

//     public String getCurrentDelinquency(final List<CIDetail> ciDetailList) {
//         if (ciDetailList != null) {
//             for (CIDetail detail : ciDetailList) {
//                 if (NON_FINANCIAL_PRODUCTS.contains(detail.getCI_PRD())) {
//                     continue;
//                 }
//                 if (!detail.getCI_SUMMRY().startsWith("0") &&
//                         !detail.getCI_SUMMRY().startsWith("C") &&
//                         !detail.getCI_SUMMRY().startsWith("D") &&
//                         !detail.getCI_SUMMRY().startsWith("N")) {
//                     return "0";
//                 }
//             }
//         }
//         return "1";
//     }


//     public String getCurrentDelinquencyText(final List<CIDetail> ciDetailList) {
//         if (ciDetailList != null) {
//             for (CIDetail detail : ciDetailList) {
//                 if (NON_FINANCIAL_PRODUCTS.contains(detail.getCI_PRD())) {
//                     continue;
//                 }
//                 if (detail.getCI_SUMMRY().startsWith("M")) {
//                     return "0";
//                 }
//             }
//         }
//         return "1";
//     }


//     public String getMaxDelinquency(final List<CIDetail> ciDetailList) {
//         List<Character> list = new ArrayList<>(Collections.singletonList('0'));
//         if (ciDetailList != null) {
//             for (CIDetail detail : ciDetailList) {
//                 if (NON_FINANCIAL_PRODUCTS.contains(detail.getCI_PRD())) {
//                     continue;
//                 }
//                 final List<Character> integerList = new ArrayList<>();
//                 for (char c : detail.getCI_SUMMRY().toCharArray()) {
//                     if (Character.isDigit(c)) {
//                         integerList.add(c);
//                     }
//                 }
//                 final Character max = Collections.max(integerList);
//                 list.add(max);
//             }
//         }
//         final Character max = Collections.max(list);
//         return String.valueOf(max);
//     }


//     public String getValidDefaults(List<Default> defaultList) {
//         if (defaultList != null) {
//             for (Default def : defaultList) {
//                 if (NON_FINANCIAL_PRODUCTS.contains(def.getDF_PRD())) {
//                     continue;
//                 }
//                 if (def.getDF_STAT().equals("OS") ||
//                         def.getDF_STAT().equals("NS") ||
//                         def.getDF_STAT().equals("PP")) {
//                     return "0";
//                 }
//             }
//         }
//         return "1";
//     }


//     //Taking DF_CUB as amount
//     public String getValidDefaultsUtil(List<Default> defaultList) {
//         BigDecimal amount = BigDecimal.ZERO;
//         if (defaultList != null) {
//             for (Default def : defaultList) {
//                 if (!NON_FINANCIAL_PRODUCTS.contains(def.getDF_PRD())) {
//                     continue;
//                 }
//                 amount = amount.add(new BigDecimal(def.getDF_CUB()));
//             }
//         }
//         return String.valueOf(amount);
//     }


//     public String getBouncedCheck(List<BouncedChecks> bouncedChecksList) {
//         if (bouncedChecksList == null || bouncedChecksList.isEmpty()) {
//             return "NB";
//         }
//         for (BouncedChecks bouncedChecks : bouncedChecksList) {
//             if (bouncedChecks.getBC_SETTLE_DATE() == null) {
//                 return "UB";
//             }
//         }
//         return "SB";
//     }


//     public String getCourtJudgement(List<Judgment> judgmentList) {
//         if (judgmentList == null || judgmentList.isEmpty()) {
//             return "NJ";
//         }
//         for (Judgment judgment : judgmentList) {
//             if (judgment.getEJ_SETTLE_DATE() == null) {
//                 return "UJ";
//             }
//         }
//         return "SJ";
//     }


//   public String getOfferLoanAmount(String monthlyNetSalaryString,
//                                      String globalDebtServicingString,
//                                      String ijarahDebtServicingString,
//                                      String tenorString,
//                                      String intRateString,
//                                      final List<Table> murabahaTableList) {
//         logger.error("calculation offer loan amount");
//         BigDecimal salary = new BigDecimal(monthlyNetSalaryString);
//         logger.error("salary: [{}]", salary);
//         BigDecimal murabahaMaxOverallDTI = BigDecimal.ZERO;
//         BigDecimal murabahaMaxInternalDTI = BigDecimal.ZERO;
//         for (Table table : murabahaTableList) {
//             if (salary.intValue() > table.getSalaryRangeMin() && salary.intValue() < table.getSalaryRangeMax()) {
//                 murabahaMaxOverallDTI = new BigDecimal(table.getMaxOverallDTI()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//                 murabahaMaxInternalDTI = new BigDecimal(table.getMaxInternalDTI()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//             }
//         }
//         logger.error("murabahaMaxOverallDTI: [{}]", murabahaMaxOverallDTI);
//         logger.error("murabahaMaxInternalDTI: [{}]", murabahaMaxInternalDTI);


//         BigDecimal globalDebtServicing = new BigDecimal(globalDebtServicingString);
//         logger.error("globalDebtServicing: [{}]", globalDebtServicing);
//         BigDecimal ijarahDebtServicing = new BigDecimal(ijarahDebtServicingString);
//         logger.error("ijarahDebtServicing: [{}]", ijarahDebtServicing);
//         BigDecimal maxAllowableGlobalDTI = murabahaMaxOverallDTI.subtract(globalDebtServicing.divide(salary, 2, RoundingMode.HALF_UP));
//         logger.error("maxAllowableGlobalDTI: [{}]", maxAllowableGlobalDTI);
//         BigDecimal maxAllowableIjarahDTI = murabahaMaxInternalDTI.subtract(ijarahDebtServicing.divide(salary, 2, RoundingMode.HALF_UP));
//         logger.error("maxAllowableIjarahDTI: [{}]", maxAllowableIjarahDTI);
//         final BigDecimal finalMaxAllowableDTI;

//         if (maxAllowableGlobalDTI.doubleValue() > maxAllowableIjarahDTI.doubleValue()) {
//             finalMaxAllowableDTI = maxAllowableIjarahDTI;
//         } else {
//             finalMaxAllowableDTI = maxAllowableGlobalDTI;
//         }
//         logger.error("finalMaxAllowableDTI: [{}]", finalMaxAllowableDTI);
//         BigDecimal maxEMI = salary.multiply(finalMaxAllowableDTI);
//         logger.error("maxEMI: [{}]", maxEMI);

//         BigDecimal tenor = new BigDecimal(tenorString);
//         logger.error("tenor: [{}]", tenor);
//         BigDecimal intRate = new BigDecimal(intRateString).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//         logger.error("intRate: [{}]", intRate);
//         BigDecimal totalPayable = tenor.multiply(maxEMI);
//         logger.error("totalPayable: [{}]", totalPayable);
//         BigDecimal loanAmt = totalPayable.divide(BigDecimal.valueOf(1).add(tenor.multiply(intRate).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)), 0, RoundingMode.HALF_UP);
//         logger.error("loanAmt: [{}]", loanAmt);
//         return String.valueOf(loanAmt);
//     }


// }
