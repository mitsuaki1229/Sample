package pinpoint

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region.US_EAST_1
import software.amazon.awssdk.services.pinpoint.PinpointClient
import software.amazon.awssdk.services.pinpoint.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

private val PROJECT_ID: String = ""
private val ACCESS_KEY: String = ""
private val SECRET_KEY: String = ""

private var pinpointClient =
    PinpointClient
        .builder()
        .region(US_EAST_1)
        .credentialsProvider({ AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY) })
        .build()

fun main(args: Array<String>) {

    println("--getSegments--------------------------------------")

    var segments = getSegments()
    printSegments(segments)

    println("--getCampaigns--------------------------------------")

    var campaigns = getCampaigns()
    printCampaigns(campaigns)

    println("--updateCampaign--------------------------------------")

    campaigns?.forEach {
        if (it.state().campaignStatus().toString() != "COMPLETED") {
            var campaignResponse = updateCampaign(it.id())
            printCampaign(campaignResponse)
        }
    }

    println("--createSegment--------------------------------------")

    var segmentResponse = createSegment()
    printSegment(segmentResponse)

    println("--createCampaign--------------------------------------")

    var campaignResponse = createCampaign(segmentResponse.id())
    printCampaign(campaignResponse)


    println("--deleteSegment--------------------------------------")

    segments?.forEach {
        var segmentResponse = deleteSegment(it.id())
        printSegment(segmentResponse)
    }

    println("--deleteCampaign--------------------------------------")

    campaigns?.forEach {
        var campaignResponse = deleteCampaign(it.id())
        printCampaign(campaignResponse)
    }
}

private fun deleteSegment(segmentId: String): SegmentResponse {
    val deleteSegmentRequest = DeleteSegmentRequest()
        .withApplicationId(PROJECT_ID)
        .withSegmentId(segmentId)

    var deleteSegmentResult = pinpointClient.deleteSegment()
    return deleteSegmentResult.segmentResponse
}

private fun deleteCampaign(campaignId: String): CampaignResponse {
    val deleteCampaignRequest = DeleteCampaignRequest()
        .withApplicationId(PROJECT_ID)
        .withCampaignId(campaignId)
    var deleteCampaignResult = pinpointClient.deleteCampaign(deleteCampaignRequest)
    return deleteCampaignResult.campaignResponse
}

private fun getSegments(): MutableList<SegmentResponse>? {
    val request = GetSegmentsRequest()
    request.applicationId = PROJECT_ID
    var getSegmentsResult = pinpointClient.getSegments(request)
    return getSegmentsResult.segmentsResponse.item
}

private fun createSegment(): SegmentResponse {
    val list: MutableList<String> = ArrayList()
    list.add("0.0.0")

    val dimension = SetDimension()
    dimension.setDimensionType(DimensionType.INCLUSIVE)
    dimension.setValues(list)

    val demographics = SegmentDemographics()
    demographics.appVersion = dimension

    val dimensions = SegmentDimensions()
    dimensions.demographic = demographics

    val writeSegmentRequest = WriteSegmentRequest()
    writeSegmentRequest.name = "createSegment-" + geRandomString()
    writeSegmentRequest.dimensions = dimensions

    val request = CreateSegmentRequest()
    request.applicationId = PROJECT_ID
    request.writeSegmentRequest = writeSegmentRequest
    var createSegmentResult = pinpointClient.createSegment(request)
    return createSegmentResult.segmentResponse
}

private fun getCampaigns(): MutableList<CampaignResponse>? {
    var request = GetCampaignsRequest()
    request.applicationId = PROJECT_ID
    var getCampaignsResult = pinpointClient.getCampaigns(request)
    return getCampaignsResult.campaignsResponse.item
}

private fun createCampaign(segmentId: String): CampaignResponse {

    val pinpointJson = object : HashMap<String?, String?>() {
        init {
            put("type", "walk")
        }
    }

    val message = Message()
    message.action = "OPEN_APP"
    message.silentPush = true
    message.jsonBody = pinpointJson.toString()

    val messageConfiguration = MessageConfiguration()
    messageConfiguration.defaultMessage = message

    val schedule = Schedule()
    schedule.startTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.JAPAN).format(Date())
    schedule.setIsLocalTime(false)
    schedule.frequency = Frequency.ONCE.toString()

    val campaignLimits = CampaignLimits()

    val writeCampaignRequest = WriteCampaignRequest()
    writeCampaignRequest.name = "createCampaign-" + geRandomString()
    writeCampaignRequest.setIsPaused(false)
    writeCampaignRequest.messageConfiguration = messageConfiguration
    writeCampaignRequest.segmentId = segmentId
    writeCampaignRequest.schedule = schedule
    writeCampaignRequest.limits = campaignLimits

    val request = CreateCampaignRequest()
        .withApplicationId(PROJECT_ID)
        .withWriteCampaignRequest(writeCampaignRequest)
    var createCampaignResult = pinpointClient.createCampaign(request)
    return createCampaignResult.campaignResponse
}

private fun updateCampaign(campaignId: String): CampaignResponse {
    val writeCampaignRequest = WriteCampaignRequest()
    writeCampaignRequest.name = geRandomString()

    var request = UpdateCampaignRequest()
    request.withApplicationId(PROJECT_ID).withCampaignId(campaignId).withWriteCampaignRequest(writeCampaignRequest)

    var updateCampaignResult = pinpointClient.updateCampaign(request)
    return updateCampaignResult.campaignResponse()
}

// tools

private fun geRandomString(): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..5)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("");
}

private fun printSegments(segments: MutableList<SegmentResponse>?) {
    segments?.forEach {
        printSegment(it)
    }
}

private fun printSegment(segment: SegmentResponse) {
    println("Segment")
    println("applicationId: " + segment.applicationId())
    println("creationDate: " + segment.creationDate())
    println("dimensions: " + segment.dimensions())
    println("id: " + segment.id())
    println("importDefinition: " + segment.importDefinition())
    println("lastModifiedDate: " + segment.lastModifiedDate())
    println("name: " + segment.name())
    println("segmentType: " + segment.segmentType())
    println("version: " + segment.version())
}

private fun printCampaigns(campaigns: MutableList<CampaignResponse>?) {
    campaigns?.forEach {
        printCampaign(it)
    }
}

private fun printCampaign(campaign: CampaignResponse) {
    println("CampaignResponse")
    println("additionalTreatments: " + campaign.additionalTreatments())
    println("applicationId: " + campaign.applicationId())
    println("creationDate: " + campaign.creationDate())
    println("defaultState: " + campaign.defaultState())
    println("description: " + campaign.description())
    println("holdoutPercent: " + campaign.holdoutPercent())
    println("id: " + campaign.id())
    println("isPaused: " + campaign.isPaused())
    println("lastModifiedDate: " + campaign.lastModifiedDate())
    println("limits: " + campaign.limits())
    println("messageConfiguration: " + campaign.messageConfiguration())
    println("name: " + campaign.name())
    println("schedule: " + campaign.schedule())
    println("segmentId: " + campaign.segmentId())
    println("segmentVersion: " + campaign.segmentVersion())
    println("state: " + campaign.state())
    println("treatmentDescription: " + campaign.treatmentDescription())
    println("treatmentName: " + campaign.treatmentName())
    println("version: " + campaign.version())
}
