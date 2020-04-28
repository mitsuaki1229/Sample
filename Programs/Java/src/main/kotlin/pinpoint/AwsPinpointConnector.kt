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
    val deleteSegmentRequest = DeleteSegmentRequest
        .builder()
        .applicationId(PROJECT_ID)
        .segmentId(segmentId)
        .build()

    var deleteSegmentResult = pinpointClient.deleteSegment(deleteSegmentRequest)
    return deleteSegmentResult.segmentResponse()
}

private fun deleteCampaign(campaignId: String): CampaignResponse {
    val deleteCampaignRequest = DeleteCampaignRequest
        .builder()
        .applicationId(PROJECT_ID)
        .campaignId(campaignId)
        .build()

    var deleteCampaignResult = pinpointClient.deleteCampaign(deleteCampaignRequest)
    return deleteCampaignResult.campaignResponse()
}

private fun getSegments(): MutableList<SegmentResponse>? {
    val request = GetSegmentsRequest
        .builder()
        .applicationId(PROJECT_ID)
        .build()

    var getSegmentsResult = pinpointClient.getSegments(request)
    return getSegmentsResult.segmentsResponse().item()
}

private fun createSegment(): SegmentResponse {
    val list: MutableList<String> = ArrayList()
    list.add("0.0.0")

    val dimension = SetDimension
        .builder()
        .dimensionType(DimensionType.INCLUSIVE)
        .values(list)
        .build()

    val demographics = SegmentDemographics
        .builder()
        .appVersion(dimension)
        .build()

    val dimensions = SegmentDimensions
        .builder()
        .demographic(demographics)
        .build()

    val writeSegmentRequest = WriteSegmentRequest
        .builder()
        .name("createSegment-" + geRandomString())
        .dimensions(dimensions)
        .build()

    val request = CreateSegmentRequest
        .builder()
        .applicationId(PROJECT_ID)
        .writeSegmentRequest(writeSegmentRequest)
        .build()

    var createSegmentResult = pinpointClient.createSegment(request)
    return createSegmentResult.segmentResponse()
}

private fun getCampaigns(): MutableList<CampaignResponse>? {
    var request = GetCampaignsRequest
        .builder()
        .applicationId(PROJECT_ID)
        .build()

    var getCampaignsResult = pinpointClient.getCampaigns(request)
    return getCampaignsResult.campaignsResponse().item()
}

private fun createCampaign(segmentId: String): CampaignResponse {

    val pinpointJson = object : HashMap<String?, String?>() {
        init {
            put("type", "walk")
        }
    }

    val message = Message
        .builder()
        .action("OPEN_APP")
        .silentPush(true)
        .jsonBody(pinpointJson.toString())
        .build()

    val messageConfiguration = MessageConfiguration
        .builder()
        .defaultMessage(message)
        .build()

    val schedule = Schedule
        .builder()
        .startTime(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.JAPAN).format(Date()))
        .isLocalTime(false)
        .frequency(Frequency.ONCE.toString())
        .build()

    val campaignLimits = CampaignLimits
        .builder()
        .build()

    val writeCampaignRequest = WriteCampaignRequest
        .builder()
        .name("createCampaign-" + geRandomString())
        .isPaused(false)
        .messageConfiguration(messageConfiguration)
        .segmentId(segmentId)
        .schedule(schedule)
        .limits(campaignLimits)
        .build()

    val request = CreateCampaignRequest
        .builder()
        .applicationId(PROJECT_ID)
        .writeCampaignRequest(writeCampaignRequest)
        .build()

    var createCampaignResult = pinpointClient.createCampaign(request)
    return createCampaignResult.campaignResponse()
}

private fun updateCampaign(campaignId: String): CampaignResponse {
    val writeCampaignRequest = WriteCampaignRequest
        .builder()
        .name(geRandomString())
        .build()

    var request = UpdateCampaignRequest
        .builder()
        .applicationId(PROJECT_ID)
        .campaignId(campaignId)
        .writeCampaignRequest(writeCampaignRequest)
        .build()

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
    println("isPaused: " + campaign.isPaused)
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
