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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import kotlin.String

object RemoteRepository {

    fun getZoneList(request: ZoneRequest): Call<ZoneResponse> {
        return RetrofitClient.apiPROD.getZoneList(request)
    }

    fun getStateList(request: StateRequest): Call<StateResponse> {
        return RetrofitClient.apiPROD.getStateList(request)
    }

    fun getCardData(request: CardDataRequest): Call<CardDataResponse> {
        return RetrofitClient.apiPROD.getCardData(request)
    }

    fun getTravelDetails(request: TravelDataRequest): Call<TravelDataResponse> {
        return RetrofitClient.apiPROD.getTravelDetailsData(request)
    }

    fun getFrmList(rgStateId: String): Call<ASMResponse> {
        val body = hashMapOf(
            "rgStateId" to rgStateId
        )
        return RetrofitClient.apiPROD.getFrmList(body)
    }

    fun getRemarkData(request: RemarkRequest): Call<RemarkResponse> {
        return RetrofitClient.apiPROD.getRemarkRecords(request)
    }

    fun getQuarterDataList(): Call<QuarterResponse> {
        return RetrofitClient.apiPROD.getQuartersData()
    }

    fun getATMDetails(atmRequest: ATMRequest): Call<ATMResponse> {
        return RetrofitClient.apiPROD.getATMDashboardData(atmRequest)
    }

    fun getAbove3StarATMData(request: Above3StarRequest): Call<Above3StarResponse> {
        return RetrofitClient.apiPROD.getAbove3StarATMDurationData(request)
    }

    fun getDashboardIcon(userCode: String): Call<DashboardIconResponse> {
        return RetrofitClient.apiPROD.getDashboardIcons(userCode)
    }

    fun uploadProfilePic(
        file: File,
        empId: String
    ): Call<UploadProfileResponse> {

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

        val multipartFile =
            MultipartBody.Part.createFormData("file", file.name, requestFile)

        val empIdBody =
            empId.toRequestBody("text/plain".toMediaTypeOrNull())

        return RetrofitClient.apiPROD.uploadProfilePic(multipartFile, empIdBody)
    }

    fun getSmartTrackAttendance(smartTrackAttendanceRequest: SmartTrackAttendanceRequest): Call<SmartTrackAttendanceResponse> {
        return RetrofitClient.apiPROD.getSmartTrackAttendanceData(smartTrackAttendanceRequest)
    }

    fun getExpenseTypeList(): Call<ExpenseTypeResponse> {
        return RetrofitClient.apiPROD.getExpenseType()
    }

    fun uploadExpenses(
        empId: RequestBody,
        amount: RequestBody,
        type: RequestBody,
        description: RequestBody,
        city: RequestBody,
        date: RequestBody,
        files: List<MultipartBody.Part>
    ): Call<ExpenseAddResponse> {

        return RetrofitClient.apiPROD.uploadExpense(
            empId,
            amount,
            type,
            description,
            city,
            date,
            files
        )
    }

    fun getExpenseList(empId: String): Call<ExpenseListResponse> {
        return RetrofitClient.apiPROD.getExpenseListed(empId)
    }


    fun getExpenseCityList(userCode: String): Call<CityResponse> {
        return RetrofitClient.apiPROD.getExpenseCityList(userCode)
    }

    fun getEmployeeAllowanceLimit(
        city: String,
        employeeId: String
    ): Call<EmployeeAllowanceResponse> {
        return RetrofitClient.apiPROD.getEmployeeAllowanceByCity(city, employeeId)
    }

    fun getApproverExpensesList(empId: String): Call<ApproverExpenseResponse> {
        return RetrofitClient.apiPROD.getApproverExpenses(empId)
    }

    fun getUpdateExpenseStatus(
        request: UpdateExpenseStatusRequest
    ): Call<UpdateExpenseStatusResponse> {

        return RetrofitClient.apiPROD.fetchUpdateExpenseStatus(request)
    }

    fun getPerKmsAmount(modeType: String, km: Int): Call<ModeExpenseResponse> {
        return RetrofitClient.apiPROD.getModeTypecalculateExpense(modeType, km)
    }

    fun getCommonList(endpoint: String): Call<CommonResponse> {
        return RetrofitClient.apiPROD.getCommonData(endpoint)
    }

    fun getStateListData(): Call<StateDataResponse> {
        return RetrofitClient.apiPROD.getStateList()
    }

    fun createSalesTracker(
        params: Map<String, RequestBody>,
        images: List<MultipartBody.Part>,
        documents: List<MultipartBody.Part>,
        voice: MultipartBody.Part?
    ): Call<SalesTrackerResponse> {

        return RetrofitClient.apiPROD.createSalesTracker(
            params,
            images,
            documents,
            voice
        )
    }

    fun getBankNames(accountTypeRequest: AccountTypeRequest): Call<BankResponse> {
        return RetrofitClient.apiPROD.submitAccountType(accountTypeRequest)
    }

    fun getSalesReport(salesTrackerRequest: SalesTrackerRequest): Call<SalesTrackerReportResponse> {
        return RetrofitClient.apiPROD.getSalesReportData(salesTrackerRequest)
    }

    fun getAccountTypeFilter(salesTrackerRequest: UserCodeRequest): Call<AccountTypeResponse> {
        return RetrofitClient.apiPROD.getAccountTypeFilter(salesTrackerRequest)
    }

    fun getBankNamesFilter(salesTrackerRequest: BankNameRequest): Call<BankNameResponse> {
        return RetrofitClient.apiPROD.getFilterBankNames(salesTrackerRequest)
    }

    fun doApprovalSalesReport(approveSalesRequest: ApproveSalesRequest): Call<SalesReportApprovalResponse> {
        return RetrofitClient.apiPROD.doApproveSalesReport(approveSalesRequest)
    }

    fun updateFollowUpDataSalesTracker(
        params: Map<String, RequestBody>,
        images: List<MultipartBody.Part>,
        documents: List<MultipartBody.Part>,
        voice: MultipartBody.Part?,

        ): Call<SalesTrackerResponse> {

        return RetrofitClient.apiPROD.updateFollowUpSalesTracker(
            params,
            images,
            documents,
            voice
        )
    }

    fun getSalesReportHistory(salesTrackerRequest: ReportHistoryRequest): Call<SalesTrackerReportResponse> {
        return RetrofitClient.apiPROD.getReportHistoryData(salesTrackerRequest)
    }

    fun getTransactionData(request: DashboardRequest): Call<CommonDashboardResponse> {
        return RetrofitClient.apiPROD.getTransactionData(request)
    }

    fun getRevenueData(request: DashboardRequest): Call<CommonDashboardResponse> {
        return RetrofitClient.apiPROD.getRevenueData(request)
    }

    fun getSSSPerformance(request: DashboardRequest): Call<SSSPerformanceResponse> {
        return RetrofitClient.apiPROD.getSSSPerformance(request)
    }

    fun getRatingData(request: DashboardRequest): Call<PsuRatingResponse> {
        return RetrofitClient.apiPROD.getRatingData(request)
    }

    fun getFrmList(request: FrmRequest): Call<FrmResponse> {
        return RetrofitClient.apiPROD.getFrmList(request)
    }

    fun getAsmDistrict(request: AsmDistrictRequest): Call<AsmDistrictResponse> {
        return RetrofitClient.apiPROD.getAsmDistrict(request)
    }

    fun getDynamicData(request: DynamicTargetRaquest): Call<DynamicTargetResponse> {
        return RetrofitClient.apiPROD.getDynamicData(request)
    }

    fun getImage(request: TargetImageRequest): Call<ResponseBody> {
        return RetrofitClient.apiPROD.getImage(request)
    }

    fun getOnboardingData(request: DashboardRequest): Call<CommonDashboardResponse> {
        return RetrofitClient.apiPROD.getOnboardingData(request)
    }

    fun getSmartTrackMapData(request: STMapRequest): Call<STMapResponse> {
        return RetrofitClient.tomcat_PROD.getSmartTrackData(request)
    }

    fun getFilterDateData(): Call<DateFilterResponse> {
        return RetrofitClient.tomcat_PROD.getDateFilterList()
    }

    fun getFilterStateData(): Call<StateFilterResponse> {
        return RetrofitClient.tomcat_PROD.getFilterStateList()
    }

    fun getFilterDistrictData(stateId: Int): Call<DistrictFilterResponse> {
        return RetrofitClient.tomcat_PROD.getFilterDistrictList(stateId)
    }

    fun getFilterEmployeeData(request: EmployeeFilterRequest): Call<EmployeeFilterResponse> {
        return RetrofitClient.tomcat_PROD.getFilterEmployeeData(request)
    }

    fun getSummarySTData(request: STSummaryRequest): Call<STSummaryResponseModel> {
        return RetrofitClient.tomcat_PROD.getSTSummaryData(request)
    }

    fun getSmartTrackSummaryDetailsData(request: STSummaryDetailsRequest): Call<STSummaryDetailsResponse> {
        return RetrofitClient.tomcat_PROD.getSTSummaryDetailsData(request)
    }

    fun getSalesTrackerDashboardSummary(request: SalesReportDashboardRequest): Call<SalesTrackerDashboardResponse> {
        return RetrofitClient.tomcat_PROD.getSalesReportSummary(request)
    }

    fun getSalesTrackerDashboardFilterState(request: SalesTrackerFilterStateRequest): Call<SalesTrackerStateResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerFilterState(request)
    }

    fun getSalesTrackerDashboardFilterAccountType(request: SalesTrackerAccountTypeRequest): Call<SalesTrackerAccountTypeResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerFilterAccountType(request)
    }

    fun getSalesTrackerDashboardFilterBanks(request: SalesTrackerFilterBankRequest): Call<SalesTrackerFilterBankResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerFilterBankName(request)
    }

    fun getSalesTrackerDashboardFilterEmployees(request: EmployeeFilterSalesTrackerRequest): Call<EmployeeFilterSalesTrackerResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerFilterEmployeeList(request)
    }

    fun getSalesTrackerDetails(request: STDetailsRequest): Call<STDetailsResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerDetailsData(request)
    }

    fun getInteractionHistoryDetails(request: InteractionHistoryRequest): Call<InteractionHistoryResponse> {
        return RetrofitClient.tomcat_PROD.getInteractionsHistory(request)
    }

    fun getSalesTrackerEmployeeDashboardSummary(request: SalesReportDashboardRequest): Call<SalesTrackerEmployeeDashboardResponse> {
        return RetrofitClient.tomcat_PROD.getEmployeeSalesSummary(request)
    }

    fun getSalesTrackerDashboardEmployeeFilterState(request: SalesTrackerFilterStateRequest): Call<SalesTrackerStateResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerEmployeeFilterState(request)
    }

    fun getSalesTrackerDashboardFilterEmployeeAccountType(request: SalesTrackerAccountTypeRequest): Call<SalesTrackerAccountTypeResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerFilterEmployeeAccountType(request)
    }

    fun getSalesTrackerDashboardFilterEmployeeBanks(request: SalesTrackerFilterBankRequest): Call<SalesTrackerFilterBankResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerFilterEmployeeBankName(request)
    }

    fun getSalesTrackerEmployeeDetails(request: STDetailsRequest): Call<STDetailsResponse> {
        return RetrofitClient.tomcat_PROD.getSalesTrackerEmployeeDetailsData(request)
    }

    fun fetchSmartTrackTaskData(request: ViewTaskRequest): Call<TaskResponse> {
        return RetrofitClient.tomcat_PROD.fetchSTTaskData(request)
    }

    fun getSmartTrackEmployeeData(request: EmployeeRequest): Call<SmartTrackerEmployeeResponse> {
        return RetrofitClient.tomcat_PROD.getSmartTrackEmployeeListAttendance(request)
    }


    fun getSmartTrackCalenderAttendanceData(request: EmployeeRequest): Call<CalenderAttendanceResponse> {
        return RetrofitClient.tomcat_PROD.getCalenderWiseAttendanceData(request)
    }

    fun getSmartTrackTimeLineData(request: EmployeeRequest): Call<SmartTrackTimeLineResponse> {
        return RetrofitClient.tomcat_UAT.getEmployeeTimeLine(request)
    }

    fun doCreateManagerTask(request: CreateMangerTaskRequest): Call<CreateManagerTaskResponse> {
        return RetrofitClient.tomcat_PROD.createMangerTask(request)
    }

    fun getTaskTypeData(): Call<TaskTypeResponse> {
        return RetrofitClient.tomcat_PROD.getTaskTypeList()
    }

    fun getDynamicFormData(screenId: String): Call<DynamicFormResponse> {
        return RetrofitClient.tomcat_PROD.getDynamicForm(screenId)
    }

    fun formDataSubmit(
        params: Map<String, RequestBody>,
        image: MultipartBody.Part
    ): Call<SubmitTaskResponse> {
        return RetrofitClient.tomcat_PROD.formDataSubmit(
            params, image
        )
    }

    fun insertLocationData(insertLocationRequest: InsertLocationRequest): Call<InsertLocationResponse> {
        return RetrofitClient.tomcat_UAT.insertMapData(
            insertLocationRequest
        )
    }

    fun getAssignedTaskData(employeeRequest: EmployeeRequest): Call<AssignedTaskResponse> {
        return RetrofitClient.tomcat_PROD.getAssignedTaskData(employeeRequest)
    }

    fun doDeleteTask(employeeRequest: EmployeeRequest): Call<AssignTaskDeleteResponse> {
        return RetrofitClient.tomcat_PROD.doDeleteTask(employeeRequest)
    }

    fun fetchTrackingSmartTrackData(trackingRequest: TrackingRequest): Call<TrackingResponse> {
        return RetrofitClient.tomcat_UAT.getTrackingSmartTrackData(trackingRequest)
    }

    fun callPunchINandOUTSmartTrack(smartTrackINOUTRequest: TrackingINandOUTRequest): Call<TrackingPunchINandOUTResponse> {
        return RetrofitClient.tomcat_UAT.callTrackingPunchINandOUT(smartTrackINOUTRequest)
    }

}
