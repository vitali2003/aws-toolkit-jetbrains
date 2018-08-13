package software.aws.toolkits.core.region

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.aws.toolkits.resources.BundledResources

class PartitionParserTest {
    @Test
    fun canLoadPartitionsFromEndpointsFile() {
        val partitions = PartitionParser.parse(BundledResources.ENDPOINTS_FILE)!!
        val awsPartition = partitions.getPartition("aws")

        val iam = awsPartition.services.getValue("iam")
        val s3 = awsPartition.services.getValue("s3")
        val lambda = awsPartition.services.getValue("lambda")

        assertThat(iam.isGlobal, equalTo(true))
        assertThat(s3.isGlobal, equalTo(false))
        assertThat(lambda.isGlobal, equalTo(false))
    }

    @Test
    fun canIListBuckets() {
        val creds = DefaultCredentialsProvider.create().resolveCredentials()
        println("Creds: keyId - ${creds.accessKeyId()}, secret - ${creds.secretAccessKey()}")
        val client = S3Client.builder().region(Region.US_WEST_2).build()
        client.listBuckets()
    }
}