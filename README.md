# S3 비동기 전송

### S3 Multipart Upload

## 요구 사항

- 상품권을 문서 형태(Excel, CSV) 형태로 변환하기를 원함.
- 변환된 상품권은 S3에 저장되어 프론트에서 가져갈 수 있도록 S3 Url를 반환 받기를 원함.
- 다수의 문서 변환 요청에도 즉각적으로 응답을 받을 수 있어야 함.

## 세부 구현

**build.gradle.kts**

```java
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	//implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	//csv
	implementation("org.apache.commons:commons-csv:1.5")
	//microsoft document processing
	implementation("org.apache.poi:poi-ooxml:4.1.2")
	//aws-java-sdk-s3
	//implementation("com.amazonaws:aws-java-sdk-s3:1.12.319")
	implementation("software.amazon.awssdk:bom:2.14.7")
	implementation("software.amazon.awssdk:s3:2.17.293")
	implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

	//kotest
	testImplementation("io.kotest:kotest-runner-junit5:5.4.0")
	testImplementation("io.kotest:kotest-assertions-core:5.4.0")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")

	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	//testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
```

- 해당 프로젝트에서 사용하고 있는 의존성 정보입니다.

**상품권 정보**

```java
@Entity
class RedeemCode(
    @Id
    @GeneratedValue
    val id: Long? = null,
    @Column
    val redeemCode: String, // 상품권 번호
    @Column
    val price: Int, // 상품권 가격
    @Column
    val reason: String // 발행 이유
)
```

- 로직을 설명하기 앞서 변환할 상품권 정보입니다.
- 해당 정보를 바탕으로 원하는 형식으로 문서를 변환할 것입니다.
    - 여기서는 두가지 형태 CSV, EXCEL 형태로 변환할 예정입니다.

### 문서 변환

- 문서는 CSV, EXCEL로 변환할 예정이며 여기서는 간단히 CSV 변환 로직만 설명하겠습니다.

**CsvRow**

```java
data class CsvRow(
    val data: List<String>
) {
    constructor(vararg data: String): this(data.toList())
}
```

- CSV에서 각 행 정보를 하나로 묶어줄 별도의 Data class입니다.

**CsvGenerator**

```java
@Component
class CsvGenerator {
    fun generate(headerTitles: Array<String>, rows: List<CsvRow>): ByteArray {
        return CSVPrinter(StringBuilder(), CSVFormat.DEFAULT.withHeader(*headerTitles)).use { csvPrinter ->
            rows.forEach { csvPrinter.printRecord(it.data) }
            csvPrinter.out.toString().toByteArray()
        }
    }
}
```

- commons-csv에서 제공하는 CSVPrinter를 사용하면 원하는 형태로 쉽게 문서를 가공할 수 있습니다.
- 가동된 정보는 ByteArray형태로 반환 되며, 이것을 S3에 업로드한 후 사용자가 해당 문서를 읽을 때는 csv형태로 읽을 수 있습니다.

### 변환 전략

- 위에서 CSV, EXCEL로 문서를 가공한다고 했는데, 현재는 두개뿐이지만 앞으로 확장성을 고려했을 때 공통적인 부분을 Interface로 추출한 후 상황에 따라 전략을 바꾸는 것이 바람직할 수 있습니다.
    - 물론 지금처럼 두개뿐인 상황에서 전략 패턴을 쓰는 것은 바람직하지 않다고 생각합니다. (불필요한 클래스 및 로직이 추가되면서 오히려 복잡성만 증가시킬 수 있습니다.)

**Strategy**

```java
package com.example.security.service.convert.strategy

import com.example.security.domain.RedeemCode
import com.example.security.service.convert.ConvertType
import kotlin.reflect.full.declaredMemberProperties

interface RedeemCodeConvertStrategy {

    fun convert(redeemCodes: List<RedeemCode>): ByteArray

    fun type(): ConvertType

    fun getRedeemCodeHeaderTitles(): Array<String> {
        return RedeemCode::class.declaredMemberProperties
            .map {
                it.name
            }
            .filter{ it != "id" }
            .toTypedArray()
    }
}
```

- 공통적으로 문서 변환 클래스가 활용할 convert를 정의했고, 해당 클래스를 구분할 목적으로 type 메서드를 선언했습니다.
- getRedeemCodeHeaderTitles()라는 RedeemCode의 헤더를 추출하는 로직을 default로 구현하여 공통적으로 활용되는 부분을 묶었습니다.
    - 사실 추상 클래스로 구현했어도 상관은 없을 것 같긴합니다.;;

**ConvertType**

```java
enum class ConvertType(
    val code: String,
    val format: String
) {
    EXCEL("01", "EXCEL"),
    CSV("02", "CSV");

    companion object {
        fun of(code: String?): ConvertType {
            if(code == null) {
                throw IllegalArgumentException("잘못된 코드 입니다.")
            }

            return ConvertType.values()
                .first { it.code == code }
        }
    }
}
```

- 각 문서 변환 타입을 구분할 목적으로 enum 클래스를 만들었습니다.

**RedeemCodeToCsvConverter**

```java
package com.example.security.service

import com.example.security.domain.RedeemCode
import com.example.security.service.convert.ConvertType
import com.example.security.service.convert.strategy.RedeemCodeConvertStrategy
import com.example.security.service.convert.generator.csv.CsvGenerator
import com.example.security.service.convert.generator.csv.CsvRow
import org.springframework.stereotype.Component

@Component
class RedeemCodeToCsvConverter(
    private val csvGenerator: CsvGenerator
) : RedeemCodeConvertStrategy {

    override fun convert(redeemCodes: List<RedeemCode>): ByteArray {
        val csvRows = redeemCodes.map {
            CsvRow(
                it.redeemCode,
                it.price.toString(),
                it.reason
            )
        }
        return csvGenerator.generate(getRedeemCodeHeaderTitles(), csvRows)
    }

    override fun type() = ConvertType.CSV
}
```

- 위의 CsvGenerator를 입력으로 받아서 RedeemCode를 row로 변환한 뒤  바이트 코드로 변환시킨 후 반환하는 메서드를 구현했습니다.

**RedeemCodeToDocumentConverter**

```java
@Component
class RedeemCodeToDocumentConverter(
    redeemCodeConvertStrategies: List<RedeemCodeConvertStrategy>
) {
    private val map: Map<ConvertType, RedeemCodeConvertStrategy> =
        redeemCodeConvertStrategies.associateBy { it.type() }

    fun convert(redeemCodes: List<RedeemCode>, convertType: ConvertType) =
        findStrategy(convertType)?.convert(redeemCodes) ?: throw IllegalStateException("no type : $convertType")

    private fun findStrategy(convertType: ConvertType) =
        map[convertType]
}
```

- 최종적으로 서비스에서 사용할 Converter 입니다.
- 전략 패턴을 활용하여 List 형태로 빈을 주입 받아 map을 구성한 뒤 요청 타입에 따라 별도의 전략을 사용하도록 구현하였습니다.

### S3 멀티 파트 업로드

- amazon s3에 업로드를 할 때 기본 파일은 정도는 사실 통째로 upload해도 큰 문제는 없습니다.
- 하지만 큰 파일 같은 경우는 chunk 단위로 쪼개서 보내야 메모리 문제를 피할 수 있습니다.
    - 참고로 각 chunk는 5MB 입니다.

**초기 설정**

```java
@Configuration
class AWSS3Config(
    @Value("\${cloud.aws.credentials.access_key}") private val awsAccessKey: String,
    @Value("\${cloud.aws.credentials.secret_key}") private val awsSecretKey: String,
    @Value("\${cloud.aws.region.static}") private val region: String
) {
    @Bean
    fun basicAwsCredentials(): AwsCredentials =
        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)

    @Bean
    fun AWSS3Client(awsCredentials: AwsCredentials): S3Client {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build()
    }

}
```

- S3Client를 사용하기전에 빈에 직접 만들어줍니다.
    - 참고로 아래 3개는 자동으로 만들어 준다고 합니다. (빈 등록이 자동으로 되어 있습니다.)
    - AmazonS3
    - AmazonS3Client
    - ResourceLoader

**application.yml**

```java
cloud:
  aws:
    credentials:
      instanceProfile: true
      access_key: #your accessKey
      secret_key: #your secretKey
    region:
      static: ap-northeast-2
    s3:
      bucket: #your bucket
    stack:
      auto: false
```

- 여기서 중요한 점은 accessKey와 secretKey 여부인데 만약 본인이 ec2환경에서 해당 s3에 접근할 수 있는 권한을 가진 유저로 수행하고 있다면 위의 instanceProfile: true를 하면 자동으로 등록되기 때문에 신경쓸 필요없습니다.
- 로컬에서 동작하는 것이라면 당연히 등록해야 하고 외부에 노출시키면 안됩니다.

**MultipartUploader**

```java
private const val TRANSFER_EXPIRE: Long = 1200L

@Component
class MultipartUploader(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucketName: String
) {
    private val parts: MutableList<CompletedPart> = arrayListOf()
    private lateinit var uploadId: String
    private lateinit var bucketkey: String

    fun initializeUpload(key: String) {
        bucketkey = key

        val createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(bucketkey)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .expires(Instant.now().plusSeconds(TRANSFER_EXPIRE))
            .build()

        val createMultipartUploadResponse = s3Client.createMultipartUpload(createMultipartUploadRequest)

        uploadId = createMultipartUploadResponse.uploadId()
    }

    fun partUpload(bytes: ByteArray) {
        val nextPartNumber: Int = parts.size + 1

        s3Client.uploadPart(
            UploadPartRequest.builder()
                .bucket(bucketName)
                .key(bucketkey)
                .uploadId(uploadId)
                .partNumber(nextPartNumber)
                .build(), RequestBody.fromBytes(bytes)
        ).also {
            parts.add(
                CompletedPart.builder()
                    .partNumber(nextPartNumber)
                    .eTag(it.eTag())
                    .build()
            )
        }
    }

    fun completeUpload() {
        CompletedMultipartUpload.builder()
            .parts(parts)
            .build()
            .also {
                CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(bucketkey)
                    .uploadId(uploadId)
                    .multipartUpload(it)
                    .build()
                    .also {
                        s3Client.completeMultipartUpload(it)
                    }
            }
    }

    fun abort() {
        s3Client.abortMultipartUpload(
            AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(bucketkey)
                .uploadId(uploadId)
                .build()
        )
    }

    private fun isInitialized() {
        if(uploadId.isEmpty()) {
            throw IllegalStateException("")
        }
    }
}
```

- 여기서 중요한 점은 initialize 가 일어난 후 반드시 completeUpload요청을 보내거나 abort 요청을 보내야 한다는 것입니다.
    - 만약 그렇지 않을 경우 과금이 될 수 있습니다.
- partUpload 코드를 보시면  part number를 넘겨주는데 해당 번호는 반드시 순차적인 것이 아니라 증가하는 순서면 문제 없습니다.
    - 참고로 1 ~ 10000사이의 숫자이어야 합니다.
- 또한 부분 전송 후 반환된 etag를 순서대로 모아서 이후 complete 요청을 보내야하기 때문에 list에 보관해줍니다.

### 최종 코드

**DocumentUploadService**

```java
private const val CHUNK_SIZE = 100000

@Component
class DocumentUploadService(
    private val converter: RedeemCodeToDocumentConverter,
    private val uploader: MultipartUploader,
    private val redeemCodeRepository: RedeemCodeRepository
) {

    @Async
    fun upload(key: String, convertType: ConvertType) {
        uploader.initializeUpload(key)
        var pages: Page<RedeemCode> = redeemCodeRepository.findAll(PageRequest.of(0, CHUNK_SIZE))
        while(!pages.isEmpty) {
            val converted = converter.convert(pages.content, convertType)
            uploader.partUpload(converted)
            if(pages.isLast) break;
            pages = redeemCodeRepository.findAll(pages.nextPageable())
        }
        uploader.completeUpload()
    }
}
```

- 저는 page단위로 전송할 필요가 있어서 이런식으로 구현했지만 본인 편하신대로 하시면 됩니다.
    - 그리고 여기서는 한 entity만 조회하기 때문에 테이블 조인등으로 인한 page 과부하 문제는 크게 일어나지 않습니다.
- 중요한 점은 위의 @Async 테크인데 이를 통해 변환 + 전송하는 로직은 모두 비동기로 처리하고사용자에게는 URL만 즉시 반환하는 형식으로 구현하여 처리하는 동안 대기하지 않도록 하였습니다.

**RedeemCodeService**

```java
@Service
class RedeemCodeService(
    private val documentUploadService: DocumentUploadService,
    private val s3UrlService: S3UrlService,
) {

    fun publish(convertType: ConvertType): String {
        val key = S3KeyGenerator.generateKey()
        documentUploadService.upload(key, convertType)
        return s3UrlService.getBucketUrl(key)
    }
}
```

---