package `in`.vakrangee.hrms.ui.travelReport.api

import `in`.vakrangee.crmCalling.model.RemarkResponse
import `in`.vakrangee.expense.request.UpdateExpenseStatusRequest
import `in`.vakrangee.expense.response.ApproverExpenseResponse
import `in`.vakrangee.expense.response.CityResponse
import `in`.vakrangee.expense.response.EmployeeAllowanceResponse
import `in`.vakrangee.expense.response.ExpenseAddResponse
import `in`.vakrangee.expense.response.ExpenseListResponse
import `in`.vakrangee.expense.response.ExpenseTypeResponse
import `in`.vakrangee.expense.response.ModeExpenseResponse
import `in`.vakrangee.expense.response.UpdateExpenseStatusResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.AccountTypeRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.AccountTypeResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.ApproveSalesRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.BankNameRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.BankNameResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.BankResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.CommonResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.EmployeeFilterSalesTrackerRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.EmployeeFilterSalesTrackerResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.InteractionHistoryRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.InteractionHistoryResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerDashboardResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.ReportHistoryRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.STDetailsRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.STDetailsResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesReportApprovalResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesReportDashboardRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerAccountTypeRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerAccountTypeResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerEmployeeDashboardResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerFilterBankRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerFilterBankResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerFilterStateRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerReportResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerRequest
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.SalesTrackerStateResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.StateDataResponse
import `in`.vakrangee.hrms.ui.salesTracker.data.UserCodeRequest
import `in`.vakrangee.hrms.ui.stManager.data.AssignTaskDeleteResponse
import `in`.vakrangee.hrms.ui.stManager.data.AssignedTaskResponse
import `in`.vakrangee.hrms.ui.stManager.data.CalenderAttendanceResponse
import `in`.vakrangee.hrms.ui.stManager.data.CreateManagerTaskResponse
import `in`.vakrangee.hrms.ui.stManager.data.CreateMangerTaskRequest
import `in`.vakrangee.hrms.ui.stManager.data.DateFilterResponse
import `in`.vakrangee.hrms.ui.stManager.data.DistrictFilterResponse
import `in`.vakrangee.hrms.ui.stManager.data.EmployeeFilterRequest
import `in`.vakrangee.hrms.ui.stManager.data.EmployeeFilterResponse
import `in`.vakrangee.hrms.ui.stManager.data.EmployeeRequest
import `in`.vakrangee.hrms.ui.stManager.data.InsertLocationRequest
import `in`.vakrangee.hrms.ui.stManager.data.InsertLocationResponse
import `in`.vakrangee.hrms.ui.stManager.data.STMapRequest
import `in`.vakrangee.hrms.ui.stManager.data.STMapResponse
import `in`.vakrangee.hrms.ui.stManager.data.STSummaryDetailsRequest
import `in`.vakrangee.hrms.ui.stManager.data.STSummaryDetailsResponse
import `in`.vakrangee.hrms.ui.stManager.data.STSummaryRequest
import `in`.vakrangee.hrms.ui.stManager.data.STSummaryResponseModel
import `in`.vakrangee.hrms.ui.stManager.data.SmartTrackTimeLineResponse
import `in`.vakrangee.hrms.ui.stManager.data.SmartTrackerEmployeeResponse
import `in`.vakrangee.hrms.ui.stManager.data.StateFilterResponse
import `in`.vakrangee.hrms.ui.stManager.data.TaskTypeResponse
import `in`.vakrangee.hrms.ui.stManager.data.TrackingRequest
import `in`.vakrangee.hrms.ui.stManager.data.TrackingResponse
import `in`.vakrangee.hrms.ui.stManager.dynamicform.data.DynamicFormResponse
import `in`.vakrangee.hrms.ui.travelReport.data.request.ATMRequest
import `in`.vakrangee.hrms.ui.travelReport.data.request.Above3StarRequest
import `in`.vakrangee.hrms.ui.travelReport.data.response.StateResponse
import `in`.vakrangee.hrms.ui.travelReport.data.request.CardDataRequest
import `in`.vakrangee.hrms.ui.travelReport.data.request.RemarkRequest
import `in`.vakrangee.hrms.ui.travelReport.data.request.SmartTrackAttendanceRequest
import `in`.vakrangee.hrms.ui.travelReport.data.request.StateRequest
import `in`.vakrangee.hrms.ui.travelReport.data.request.TravelDataRequest
import `in`.vakrangee.hrms.ui.travelReport.data.request.ZoneRequest
import `in`.vakrangee.hrms.ui.travelReport.data.response.ASMResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.ATMResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.Above3StarResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.CardDataResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.DashboardIconResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.QuarterResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.SmartTrackAttendanceResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.TravelDataResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.UploadProfileResponse
import `in`.vakrangee.hrms.ui.travelReport.data.response.ZoneResponse
import `in`.vakrangee.psuBank.model.request.AsmDistrictRequest
import `in`.vakrangee.psuBank.model.request.DashboardRequest
import `in`.vakrangee.psuBank.model.request.DynamicTargetRaquest
import `in`.vakrangee.psuBank.model.request.FrmRequest
import `in`.vakrangee.psuBank.model.request.TargetImageRequest
import `in`.vakrangee.psuBank.model.response.AsmDistrictResponse
import `in`.vakrangee.psuBank.model.response.CommonDashboardResponse
import `in`.vakrangee.psuBank.model.response.DynamicTargetResponse
import `in`.vakrangee.psuBank.model.response.FrmResponse
import `in`.vakrangee.psuBank.model.response.PsuRatingResponse
import `in`.vakrangee.psuBank.model.response.SSSPerformanceResponse
import `in`.vakrangee.smartTrack.request.TrackingINandOUTRequest
import `in`.vakrangee.smartTrack.request.TrackingPunchINandOUTResponse
import `in`.vakrangee.smartTrack.request.ViewTaskRequest
import `in`.vakrangee.smartTrack.response.SubmitTaskResponse
import `in`.vakrangee.smartTrack.response.TaskResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface APIEndPoints {

    @POST("travel/card-data")
    fun getCardData(
        @Body request: CardDataRequest
    ): Call<CardDataResponse>

    @POST("travel/data-list")
    fun getTravelDetailsData(
        @Body request: TravelDataRequest
    ): Call<TravelDataResponse>

    @POST("api/atm/zoneList")
    fun getZoneList(
        @Body request: ZoneRequest
    ): Call<ZoneResponse>

    @POST("api/atm/rgStateListWithZoneId")
    fun getStateList(
        @Body request: StateRequest
    ): Call<StateResponse>

    @POST("api/atm/frmlist") //asm list
    fun getFrmList(
        @Body body: Map<String, String>
    ): Call<ASMResponse>

    @POST("api/atm/call-records")
    fun getRemarkRecords(
        @Body request: RemarkRequest
    ): Call<RemarkResponse>

    @GET("api/atm/tech-live-quarters")
    fun getQuartersData(
    ): Call<QuarterResponse>

    @POST("api/atm/atm-Dashboard-Main")
    fun getATMDashboardData(
        @Body request: ATMRequest
    ): Call<ATMResponse>

    @POST("api/atm/calling-above3Star-details")
    fun getAbove3StarATMDurationData(
        @Body request: Above3StarRequest
    ): Call<Above3StarResponse>

    @GET("getExpenseTypes")
    fun getExpenseType(
    ): Call<ExpenseTypeResponse>

    @Multipart
    @POST("expenses-add")
    fun uploadExpense(
        @Part("emp_id") empId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("type") type: RequestBody,
        @Part("description") description: RequestBody,
        @Part("city_name") cityName: RequestBody,
        @Part("date") date: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Call<ExpenseAddResponse>

    @GET("expenses-list")
    fun getExpenseListed(
        @Query("emp_id") empId: String
    ): Call<ExpenseListResponse>


    @GET("api/dashboard/icon")
    fun getDashboardIcons(
        @Query("userCode") userCode: String
    ): Call<DashboardIconResponse>

    @Multipart
    @POST("api/dashboard/upload-profile-pic")
    fun uploadProfilePic(
        @Part file: MultipartBody.Part,
        @Part("empId") empId: RequestBody
    ): Call<UploadProfileResponse>

    @GET("getExpenseCityList")
    fun getExpenseCityList(
        @Query("userCode") userCode: String
    ): Call<CityResponse>


    @GET("getEmployeeTADAByCity")
    fun getEmployeeAllowanceByCity(
        @Query("city") city: String,
        @Query("employeeId") employeeId: String
    ): Call<EmployeeAllowanceResponse>

    @GET("approver-list")
    fun getApproverExpenses(
        @Query("emp_id") empId: String
    ): Call<ApproverExpenseResponse>

    @POST("expense-updateStatus")
    fun fetchUpdateExpenseStatus(
        @Body request: UpdateExpenseStatusRequest
    ): Call<UpdateExpenseStatusResponse>

    @GET("modeTypecalculateExpense")
    fun getModeTypecalculateExpense(
        @Query("modeType") modeType: String,
        @Query("km") km: Int
    ): Call<ModeExpenseResponse>


    @POST("SmartTrack/track/attendance")
    fun getSmartTrackAttendanceData(
        @Body smartTrackAttendanceRequest: SmartTrackAttendanceRequest
    ): Call<SmartTrackAttendanceResponse>


    @GET
    fun getCommonData(
        @Url url: String
    ): Call<CommonResponse>

    @GET("api-super/stateNameList")
    fun getStateList(
    ): Call<StateDataResponse>


    @Multipart
    @POST("sales-tracker/create")
    fun createSalesTracker(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>,
        @Part documents: List<MultipartBody.Part>,
        @Part voice: MultipartBody.Part?

    ): Call<SalesTrackerResponse>


    @POST("sales-tracker/bankNamelist")
    fun submitAccountType(
        @Body request: AccountTypeRequest
    ): Call<BankResponse>

    @POST("sales-tracker/getData")
    fun getSalesReportData(
        @Body request: SalesTrackerRequest
    ): Call<SalesTrackerReportResponse>

    @POST("sales-tracker/getAccountNameType")
    fun getAccountTypeFilter(
        @Body request: UserCodeRequest
    ): Call<AccountTypeResponse>

    @POST("sales-tracker/getBankName")
    fun getFilterBankNames(
        @Body request: BankNameRequest
    ): Call<BankNameResponse>

    @POST("sales-tracker/approve")
    fun doApproveSalesReport(
        @Body request: ApproveSalesRequest
    ): Call<SalesReportApprovalResponse>

    @Multipart
    @POST("sales-tracker/update")
    fun updateFollowUpSalesTracker(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>,
        @Part documents: List<MultipartBody.Part>,
        @Part voice: MultipartBody.Part?

    ): Call<SalesTrackerResponse>

    @POST("sales-tracker/getInteractionHistory")
    fun getReportHistoryData(
        @Body request: ReportHistoryRequest
    ): Call<SalesTrackerReportResponse>

    @POST("SmartTrack/track/data")
    fun getSmartTrackData(
        @Body request: STMapRequest
    ): Call<STMapResponse>

    @GET("api/date-filters")
    fun getDateFilterList(
    ): Call<DateFilterResponse>

    @GET("api/state/all")
    fun getFilterStateList(
    ): Call<StateFilterResponse>

    @GET("api/district/{id}")
    fun getFilterDistrictList(
        @Path("id") stateId: Int
    ): Call<DistrictFilterResponse>

    @POST("api/employee/hierarchy")
    fun getFilterEmployeeData(
        @Body request: EmployeeFilterRequest
    ): Call<EmployeeFilterResponse>

    @POST("api/dashboard/summary")
    fun getSTSummaryData(
        @Body request: STSummaryRequest
    ): Call<STSummaryResponseModel>

    @POST("api/dashboard/details")
    fun getSTSummaryDetailsData(
        @Body request: STSummaryDetailsRequest
    ): Call<STSummaryDetailsResponse>

    @POST("sales/tracker/dashboard")
    fun getSalesReportSummary(
        @Body request: SalesReportDashboardRequest
    ): Call<SalesTrackerDashboardResponse>

    @POST("salestracker/filter/states")
    fun getSalesTrackerFilterState(
        @Body request: SalesTrackerFilterStateRequest
    ): Call<SalesTrackerStateResponse>

    @POST("salestracker/filter/account-types")
    fun getSalesTrackerFilterAccountType(
        @Body request: SalesTrackerAccountTypeRequest
    ): Call<SalesTrackerAccountTypeResponse>

    @POST("salestracker/filter/bankNameList")
    fun getSalesTrackerFilterBankName(
        @Body request: SalesTrackerFilterBankRequest
    ): Call<SalesTrackerFilterBankResponse>

    @POST("salestracker/filter/empList")
    fun getSalesTrackerFilterEmployeeList(
        @Body request: EmployeeFilterSalesTrackerRequest
    ): Call<EmployeeFilterSalesTrackerResponse>


    @POST("sales/tracker/details")
    fun getSalesTrackerDetailsData(
        @Body request: STDetailsRequest
    ): Call<STDetailsResponse>

    @POST("sales/tracker/getInteractionHistory")
    fun getInteractionsHistory(
        @Body request: InteractionHistoryRequest
    ): Call<InteractionHistoryResponse>


    @POST("sales/tracker/emp/dashboard")
    fun getEmployeeSalesSummary(
        @Body request: SalesReportDashboardRequest
    ): Call<SalesTrackerEmployeeDashboardResponse>

    @POST("sales/tracker/emp/states")
    fun getSalesTrackerEmployeeFilterState(
        @Body request: SalesTrackerFilterStateRequest
    ): Call<SalesTrackerStateResponse>

    @POST("sales/tracker/emp/account-types")
    fun getSalesTrackerFilterEmployeeAccountType(
        @Body request: SalesTrackerAccountTypeRequest
    ): Call<SalesTrackerAccountTypeResponse>

    @POST("sales/tracker/emp/bankNameList")
    fun getSalesTrackerFilterEmployeeBankName(
        @Body request: SalesTrackerFilterBankRequest
    ): Call<SalesTrackerFilterBankResponse>


    @POST("sales/tracker/emp/details")
    fun getSalesTrackerEmployeeDetailsData(
        @Body request: STDetailsRequest
    ): Call<STDetailsResponse>


    @Headers("API-KEY: mysecret2939839829jdbfjdsbfjbdf9823jhbjfbdbif")
    @POST("api/onboardingdata")
    fun getOnboardingData(
        @Body request: DashboardRequest
    ): Call<CommonDashboardResponse>


    @Headers("API-KEY: mysecret2939839829jdbfjdsbfjbdf9823jhbjfbdbif")
    @POST("api/Transaction")
    fun getTransactionData(
        @Body request: DashboardRequest
    ): Call<CommonDashboardResponse>


    @Headers("API-KEY: mysecret2939839829jdbfjdsbfjbdf9823jhbjfbdbif")
    @POST("api/Revenue")
    fun getRevenueData(
        @Body request: DashboardRequest
    ): Call<CommonDashboardResponse>


    @Headers("API-KEY: mysecret2939839829jdbfjdsbfjbdf9823jhbjfbdbif")
    @POST("api/SSSperformance")
    fun getSSSPerformance(
        @Body request: DashboardRequest
    ): Call<SSSPerformanceResponse>


    @Headers("API-KEY: mysecret2939839829jdbfjdsbfjbdf9823jhbjfbdbif")
    @POST("api/Starratingdata")
    fun getRatingData(
        @Body request: DashboardRequest
    ): Call<PsuRatingResponse>

    @POST("api/get-banking-asm")
    fun getFrmList(
        @Body request: FrmRequest
    ): Call<FrmResponse>


    @POST("api/get-asm-district")
    fun getAsmDistrict(
        @Body request: AsmDistrictRequest
    ): Call<AsmDistrictResponse>


    @Headers("API-KEY: mysecret2939839829jdbfjdsbfjbdf9823jhbjfbdbif")
    @POST("api/dynamictarget")
    fun getDynamicData(
        @Body request: DynamicTargetRaquest
    ): Call<DynamicTargetResponse>

    @Headers("API-KEY: mysecret2939839829jdbfjdsbfjbdf9823jhbjfbdbif")
    @POST("api/targetImage")
    fun getImage(
        @Body request: TargetImageRequest
    ): Call<ResponseBody>

    @POST("track/task-view")
    fun fetchSTTaskData(
        @Body request: ViewTaskRequest
    ): Call<TaskResponse>

    @POST("api/myEmployee")
    fun getSmartTrackEmployeeListAttendance(
        @Body request: EmployeeRequest
    ): Call<SmartTrackerEmployeeResponse>

    @POST("api/myAttendance")
    fun getCalenderWiseAttendanceData(
        @Body request: EmployeeRequest
    ): Call<CalenderAttendanceResponse>

    @POST("api/travel-employee-distance")
    fun getEmployeeTimeLine(
        @Body request: EmployeeRequest
    ): Call<SmartTrackTimeLineResponse>

    @POST("api/createAssignedTask")
    fun createMangerTask(
        @Body request: CreateMangerTaskRequest
    ): Call<CreateManagerTaskResponse>

    @GET("track/form-Screens")
    fun getTaskTypeList(
    ): Call<TaskTypeResponse>

    @GET("track/form/{screenId}")
    fun getDynamicForm(
        @Path(value = "screenId", encoded = true)
        screenId: String
    ): Call<DynamicFormResponse>

    @Multipart
    @POST("track/submit-task")
    fun formDataSubmit(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part
    ): Call<SubmitTaskResponse>

    @POST("track/data-insert")
    fun insertMapData(
       @Body insertLocationRequest: InsertLocationRequest
    ): Call<InsertLocationResponse>

    @POST("api/taskCreatedByManager")
    fun getAssignedTaskData(
       @Body employeeRequest: EmployeeRequest
    ): Call<AssignedTaskResponse>

    @POST("api/deleteTask")
    fun doDeleteTask(
       @Body employeeRequest: EmployeeRequest
    ): Call<AssignTaskDeleteResponse>

    @POST("track/data")
    fun getTrackingSmartTrackData(
       @Body trackingRequest: TrackingRequest
    ): Call<TrackingResponse>

    @POST("track/logging-new")
    fun callTrackingPunchINandOUT(@Body request: TrackingINandOUTRequest
    ): Call<TrackingPunchINandOUTResponse>
}
