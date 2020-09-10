package org.gbif.pipelines.core.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gbif.api.vocabulary.AgentIdentifierType;
import org.gbif.api.vocabulary.Extension;
import org.gbif.api.vocabulary.License;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.api.vocabulary.OccurrenceStatus;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifInternalTerm;
import org.gbif.pipelines.io.avro.AgentIdentifier;
import org.gbif.pipelines.io.avro.Amplification;
import org.gbif.pipelines.io.avro.AmplificationRecord;
import org.gbif.pipelines.io.avro.AudubonRecord;
import org.gbif.pipelines.io.avro.BasicRecord;
import org.gbif.pipelines.io.avro.BlastResult;
import org.gbif.pipelines.io.avro.DeterminedDate;
import org.gbif.pipelines.io.avro.EventDate;
import org.gbif.pipelines.io.avro.ExtendedRecord;
import org.gbif.pipelines.io.avro.GadmFeatures;
import org.gbif.pipelines.io.avro.ImageRecord;
import org.gbif.pipelines.io.avro.LocationFeatureRecord;
import org.gbif.pipelines.io.avro.LocationRecord;
import org.gbif.pipelines.io.avro.MachineTag;
import org.gbif.pipelines.io.avro.MeasurementOrFact;
import org.gbif.pipelines.io.avro.MeasurementOrFactRecord;
import org.gbif.pipelines.io.avro.MediaType;
import org.gbif.pipelines.io.avro.MetadataRecord;
import org.gbif.pipelines.io.avro.Multimedia;
import org.gbif.pipelines.io.avro.MultimediaRecord;
import org.gbif.pipelines.io.avro.Rank;
import org.gbif.pipelines.io.avro.RankedName;
import org.gbif.pipelines.io.avro.TaggedValueRecord;
import org.gbif.pipelines.io.avro.TaxonRecord;
import org.gbif.pipelines.io.avro.TemporalRecord;
import org.junit.Test;

public class GbifJsonConverterTest {

  @Test
  public void jsonFromSpecificRecordBaseTest() {
    // State
    Map<String, String> erMap = new HashMap<>(2);
    erMap.put("http://rs.tdwg.org/dwc/terms/locality", "something:{something}");
    erMap.put("http://purl.org/dc/terms/remark", "{\"something\":1}{\"something\":1}");
    erMap.put(DwcTerm.recordedBy.qualifiedName(), "Jeremia garde \u001Eà elfutsone");
    erMap.put(DwcTerm.identifiedBy.qualifiedName(), "D2\u001fR2");
    erMap.put(DwcTerm.footprintWKT.qualifiedName(), "footprintWKTfootprintWKTfootprintWKT");

    MetadataRecord mr =
        MetadataRecord.newBuilder()
            .setId("777")
            .setCrawlId(1)
            .setDatasetKey("datatesKey")
            .setLicense(License.CC0_1_0.name())
            .setMachineTags(
                Collections.singletonList(
                    MachineTag.newBuilder()
                        .setName("Name")
                        .setNamespace("Namespace")
                        .setValue("Value")
                        .build()))
            .build();

    ExtendedRecord er =
        ExtendedRecord.newBuilder().setId("777").setCoreRowType("core").setCoreTerms(erMap).build();

    BasicRecord br =
        BasicRecord.newBuilder()
            .setId("777")
            .setGbifId(111L)
            .setOrganismQuantity(2d)
            .setOrganismQuantityType("OrganismQuantityType")
            .setSampleSizeUnit("SampleSizeUnit")
            .setSampleSizeValue(2d)
            .setRelativeOrganismQuantity(0.001d)
            .setLicense(License.CC_BY_NC_4_0.name())
            .setOccurrenceStatus(OccurrenceStatus.PRESENT.name())
            .setRecordedByIds(
                Collections.singletonList(
                    AgentIdentifier.newBuilder()
                        .setType(AgentIdentifierType.OTHER.name())
                        .setValue("someId")
                        .build()))
            .setIdentifiedByIds(
                Collections.singletonList(
                    AgentIdentifier.newBuilder()
                        .setType(AgentIdentifierType.OTHER.name())
                        .setValue("someId")
                        .build()))
            .build();

    TemporalRecord tmr =
        TemporalRecord.newBuilder()
            .setId("777")
            .setCreated(0L)
            .setEventDate(EventDate.newBuilder().setLte("01-01-2018").setGte("01-01-2011").build())
            .setDay(1)
            .setMonth(1)
            .setYear(2011)
            .setStartDayOfYear(1)
            .build();
    tmr.getIssues().getIssueList().add(OccurrenceIssue.ZERO_COORDINATE.name());

    LocationRecord lr =
        LocationRecord.newBuilder()
            .setId("777")
            .setCreated(1555454275758L)
            .setCountry("Country")
            .setCountryCode("Code 1'2\"")
            .setDecimalLatitude(1d)
            .setDecimalLongitude(2d)
            .setContinent("something{something}")
            .setLocality("[68]")
            .setGadm(
                GadmFeatures.newBuilder()
                    .setLevel2Gid("XAA.1.2_1")
                    .setLevel2Name("Muni Cipality")
                    .build())
            .build();
    lr.getIssues().getIssueList().add(OccurrenceIssue.BASIS_OF_RECORD_INVALID.name());

    List<RankedName> rankedNameList = new ArrayList<>();
    RankedName synonym =
        RankedName.newBuilder().setKey(10).setName("synonym").setRank(Rank.SPECIES).build();
    RankedName au =
        RankedName.newBuilder().setKey(11).setName("accepted usage").setRank(Rank.SPECIES).build();
    RankedName name =
        RankedName.newBuilder().setKey(1).setName("Name").setRank(Rank.CHEMOFORM).build();
    RankedName name2 =
        RankedName.newBuilder().setKey(2).setName("Name2").setRank(Rank.ABERRATION).build();
    rankedNameList.add(name);
    rankedNameList.add(name2);

    TaxonRecord tr =
        TaxonRecord.newBuilder()
            .setId("777")
            .setAcceptedUsage(au)
            .setClassification(rankedNameList)
            .setUsage(synonym)
            .build();

    TaggedValueRecord tvr =
        TaggedValueRecord.newBuilder()
            .setId("123")
            .setTaggedValues(
                new ImmutableMap.Builder<String, String>()
                    .put(
                        GbifInternalTerm.collectionKey.qualifiedName(),
                        "75956ee6-1a2b-4fa3-b3e8-ccda64ce6c2d")
                    .put(
                        GbifInternalTerm.institutionKey.qualifiedName(),
                        "6ac3f774-d9fb-4796-b3e9-92bf6c81c084")
                    .build())
            .build();

    // When
    ObjectNode result = GbifJsonConverter.toJson(mr, er, tmr, lr, tr, br, tvr);

    // Should
    assertTrue(JsonValidationUtils.isValid(result.toString()));

    assertEquals(mr.getDatasetKey(), result.path("datasetKey").asText());
    assertEquals(mr.getCrawlId(), (Integer) result.path("crawlId").asInt());
    assertEquals("CC_BY_NC_4_0", result.path("license").asText());
    assertEquals(mr.getId(), result.path("id").asText());
    assertEquals("Jeremia garde ,à elfutsone", result.path("recordedBy").asText());
    assertEquals("D2 R2", result.path("identifiedBy").asText());
    assertEquals("01-01-2011", result.path("eventDateSingle").asText());
    assertEquals("2011", result.path("year").asText());
    assertEquals("1", result.path("month").asText());
    assertEquals("1", result.path("day").asText());
    assertEquals(
        "{\"gte\":\"01-01-2011\",\"lte\":\"01-01-2018\"}", result.path("eventDate").toString());
    assertEquals("1", result.path("startDayOfYear").asText());
    assertEquals("{\"lon\":2.0,\"lat\":1.0}", result.path("coordinates").toString());
    assertEquals("1.0", result.path("decimalLatitude").asText());
    assertEquals("2.0", result.path("decimalLongitude").asText());
    assertEquals("POINT (2.0 1.0)", result.path("scoordinates").asText());
    assertEquals("Country", result.path("country").asText());
    assertEquals("Code 1'2\"", result.path("countryCode").asText());
    assertEquals("[68]", result.path("locality").asText());
    assertEquals(
        "{\"level2Gid\":\"XAA.1.2_1\",\"level2Name\":\"Muni Cipality\"}",
        result.path("gadm").toString());

    String expectedAll =
        "[\"Jeremia garde ,à elfutsone\",\"{\\\"something\\\":1}{\\\"something\\\":1}\",\"D2 R2\",\"something:{something}\"]";
    assertEquals(expectedAll, result.path("all").toString());

    String expectedVerbatim =
        "{\"core\":"
            + "{\"http://rs.tdwg.org/dwc/terms/identifiedBy\":\"D2 R2\","
            + "\"http://rs.tdwg.org/dwc/terms/footprintWKT\":\"footprintWKTfootprintWKTfootprintWKT\","
            + "\"http://purl.org/dc/terms/remark\":\"{\\\"something\\\":1}{\\\"something\\\":1}\","
            + "\"http://rs.tdwg.org/dwc/terms/recordedBy\":\"Jeremia garde ,à elfutsone\","
            + "\"http://rs.tdwg.org/dwc/terms/locality\":\"something:{something}\"},"
            + "\"extensions\":{}}";
    assertEquals(expectedVerbatim, result.path("verbatim").toString());

    String expectedGbifClassification =
        "{"
            + "\"usage\":{\"key\":10,\"name\":\"synonym\",\"rank\":\"SPECIES\"},"
            + "\"classification\":[" // gbifClassification.classification
            + "{\"key\":1,\"name\":\"Name\",\"rank\":\"CHEMOFORM\"},"
            + "{\"key\":2,\"name\":\"Name2\",\"rank\":\"ABERRATION\"}"
            + "]," // end gbifClassification.classification
            + "\"acceptedUsage\":{\"key\":11,\"name\":\"accepted usage\",\"rank\":\"SPECIES\"},"
            + "\"chemoformKey\":1,"
            + "\"chemoform\":\"Name\","
            + "\"aberrationKey\":2,"
            + "\"aberration\":\"Name2\","
            + "\"classificationPath\":\"_1_2\","
            + "\"taxonKey\":[1,2,10,11]"
            + "}";
    assertEquals(expectedGbifClassification, result.path("gbifClassification").toString());

    assertEquals("111", result.path("gbifId").asText());
    assertEquals("2.0", result.path("sampleSizeValue").asText());
    assertEquals("SampleSizeUnit", result.path("sampleSizeUnit").asText());
    assertEquals("2.0", result.path("organismQuantity").asText());
    assertEquals("OrganismQuantityType", result.path("organismQuantityType").asText());
    assertEquals("0.001", result.path("relativeOrganismQuantity").asText());
    assertEquals(
        "[{\"type\":\"OTHER\",\"value\":\"someId\"}]", result.path("identifiedByIds").toString());
    assertEquals(
        "[{\"type\":\"OTHER\",\"value\":\"someId\"}]", result.path("recordedByIds").toString());
    assertEquals("PRESENT", result.path("occurrenceStatus").asText());
    assertEquals("75956ee6-1a2b-4fa3-b3e8-ccda64ce6c2d", result.path("collectionKey").asText());
    assertEquals("6ac3f774-d9fb-4796-b3e9-92bf6c81c084", result.path("institutionKey").asText());

    String expectedIssues = "[\"BASIS_OF_RECORD_INVALID\",\"ZERO_COORDINATE\"]";
    assertEquals(expectedIssues, result.path("issues").toString());
    assertEquals(
        OccurrenceIssue.values().length - expectedIssues.split(",").length,
        result.path("notIssues").size());
    assertEquals("2019-04-16T22:37:55.758", result.path("created").asText());
  }

  @Test
  public void jsonFromSpecificRecordBaseAustraliaTest() {
    // State
    Map<String, String> erMap = new HashMap<>(2);
    erMap.put("http://rs.tdwg.org/dwc/terms/locality", "something:{something}");
    erMap.put("http://rs.tdwg.org/dwc/terms/remark", "{\"something\":1}{\"something\":1}");

    // State
    Map<String, String> ext1 = new HashMap<>();
    ext1.put(DcTerm.identifier.qualifiedName(), "http://www.gbif.org/tmp.jpg");
    ext1.put(DcTerm.references.qualifiedName(), "http://www.gbif.org/tmp.jpg");
    ext1.put(DcTerm.created.qualifiedName(), "2010");
    ext1.put(DcTerm.title.qualifiedName(), "Tt1");
    ext1.put(DcTerm.description.qualifiedName(), "Desc1");
    ext1.put(DcTerm.spatial.qualifiedName(), "Sp1");
    ext1.put(DcTerm.format.qualifiedName(), "jpeg");
    ext1.put(DcTerm.creator.qualifiedName(), "Cr1");
    ext1.put(DcTerm.contributor.qualifiedName(), "Cont1");
    ext1.put(DcTerm.publisher.qualifiedName(), "Pub1");
    ext1.put(DcTerm.audience.qualifiedName(), "Aud1");
    ext1.put(DcTerm.license.qualifiedName(), "Lic1");
    ext1.put(DcTerm.rightsHolder.qualifiedName(), "Rh1");
    ext1.put(DwcTerm.datasetID.qualifiedName(), "1");
    ext1.put("http://www.w3.org/2003/01/geo/wgs84_pos#longitude", "-131.3");
    ext1.put("http://www.w3.org/2003/01/geo/wgs84_pos#latitude", "60.4");

    Map<String, String> ext2 = new HashMap<>();
    ext2.put(DcTerm.created.qualifiedName(), "not a date");

    Map<String, List<Map<String, String>>> extMap1 = new HashMap<>();
    extMap1.put(Extension.IMAGE.getRowType(), Arrays.asList(ext1, ext2));

    Map<String, List<Map<String, String>>> extMap2 = new HashMap<>();
    extMap2.put(Extension.AUDUBON.getRowType(), Arrays.asList(ext1, ext2));

    extMap1.putAll(extMap2);

    ExtendedRecord er =
        ExtendedRecord.newBuilder()
            .setId("777")
            .setCoreRowType("core")
            .setCoreTerms(erMap)
            .setExtensions(extMap1)
            .build();

    TemporalRecord tmr =
        TemporalRecord.newBuilder()
            .setId("777")
            .setEventDate(EventDate.newBuilder().setLte("01-01-2018").setGte("01-01-2011").build())
            .setDay(1)
            .setMonth(1)
            .setYear(2011)
            .setStartDayOfYear(1)
            .build();
    tmr.getIssues().getIssueList().add(OccurrenceIssue.ZERO_COORDINATE.name());

    LocationRecord lr =
        LocationRecord.newBuilder()
            .setId("777")
            .setCountry("Country")
            .setCountryCode("Code 1'2\"")
            .setDecimalLatitude(1d)
            .setDecimalLongitude(2d)
            .setContinent("something{something}")
            .build();
    lr.getIssues().getIssueList().add(OccurrenceIssue.BASIS_OF_RECORD_INVALID.name());

    LocationFeatureRecord asr =
        LocationFeatureRecord.newBuilder()
            .setId("777")
            .setItems(Collections.singletonMap("data", "value"))
            .build();

    List<RankedName> rankedNameList = new ArrayList<>();
    RankedName name =
        RankedName.newBuilder().setKey(1).setName("Name").setRank(Rank.CHEMOFORM).build();
    RankedName name2 =
        RankedName.newBuilder().setKey(2).setName("Name2").setRank(Rank.ABERRATION).build();
    rankedNameList.add(name);
    rankedNameList.add(name2);

    TaxonRecord tr =
        TaxonRecord.newBuilder()
            .setId("777")
            .setClassification(rankedNameList)
            .setUsage(name2)
            .build();

    MeasurementOrFactRecord mfr =
        MeasurementOrFactRecord.newBuilder()
            .setId("777")
            .setMeasurementOrFactItems(
                Arrays.asList(
                    MeasurementOrFact.newBuilder()
                        .setType("{\"something\":1}{\"something\":1}")
                        .setId("123")
                        .setValueParsed(1.1d)
                        .setDeterminedDateParsed(
                            DeterminedDate.newBuilder().setGte("2010").setLte("2011").build())
                        .build(),
                    MeasurementOrFact.newBuilder()
                        .setId("124")
                        .setDeterminedDateParsed(
                            DeterminedDate.newBuilder().setGte("2010").setLte("2012").build())
                        .build(),
                    MeasurementOrFact.newBuilder().setId("125").build()))
            .build();

    // When
    ObjectNode result = GbifJsonConverter.toJson(er, tmr, lr, tr, asr, mfr);

    // Should
    assertTrue(JsonValidationUtils.isValid(result.toString()));
    assertEquals(er.getId(), result.path("id").asText());
    assertEquals("01-01-2011", result.path("eventDateSingle").asText());
    assertEquals("2011", result.path("year").asText());
    assertEquals("1", result.path("month").asText());
    assertEquals("1", result.path("day").asText());
    assertEquals(
        "{\"gte\":\"01-01-2011\",\"lte\":\"01-01-2018\"}", result.path("eventDate").toString());
    assertEquals("1", result.path("startDayOfYear").asText());
    assertEquals("{\"lon\":2.0,\"lat\":1.0}", result.path("coordinates").toString());
    assertEquals("1.0", result.path("decimalLatitude").asText());
    assertEquals("2.0", result.path("decimalLongitude").asText());
    assertEquals("POINT (2.0 1.0)", result.path("scoordinates").asText());
    assertEquals("something{something}", result.path("continent").asText());
    assertEquals("Country", result.path("country").asText());
    assertEquals("Code 1'2\"", result.path("countryCode").asText());

    String expectedAll =
        "[\"Cr1\",\"{\\\"something\\\":1}{\\\"something\\\":1}\","
            + "\"http://www.gbif.org/tmp.jpg\",\"something:{something}\",\"2010\",\"Desc1\","
            + "\"Lic1\",\"Tt1\",\"1\",\"Pub1\",\"-131.3\",\"Sp1\",\"not a date\",\"60.4\","
            + "\"jpeg\",\"Rh1\",\"Cont1\",\"Aud1\""
            + "]";
    assertEquals(expectedAll, result.path("all").toString());

    String expectedVerbatim =
        "{\"core\":{" // verbatim.core
            + "\"http://rs.tdwg.org/dwc/terms/remark\":"
            + "\"{\\\"something\\\":1}{\\\"something\\\":1}\","
            + "\"http://rs.tdwg.org/dwc/terms/locality\":\"something:{something}\""
            + "}," // end verbatim.core
            + "\"extensions\":{" // verbatim.extensions
            + "\"http://rs.tdwg.org/ac/terms/Multimedia\":[" // verbatim.extensions.Multimedia
            + "{\"http://purl.org/dc/terms/license\":\"Lic1\","
            + "\"http://www.w3.org/2003/01/geo/wgs84_pos#latitude\":\"60.4\","
            + "\"http://purl.org/dc/terms/identifier\":\"http://www.gbif.org/tmp.jpg\","
            + "\"http://rs.tdwg.org/dwc/terms/datasetID\":\"1\","
            + "\"http://purl.org/dc/terms/description\":\"Desc1\","
            + "\"http://purl.org/dc/terms/publisher\":\"Pub1\","
            + "\"http://purl.org/dc/terms/audience\":\"Aud1\","
            + "\"http://purl.org/dc/terms/spatial\":\"Sp1\","
            + "\"http://purl.org/dc/terms/format\":\"jpeg\","
            + "\"http://purl.org/dc/terms/rightsHolder\":\"Rh1\","
            + "\"http://purl.org/dc/terms/creator\":\"Cr1\","
            + "\"http://purl.org/dc/terms/created\":\"2010\","
            + "\"http://purl.org/dc/terms/references\":\"http://www.gbif.org/tmp.jpg\","
            + "\"http://purl.org/dc/terms/contributor\":\"Cont1\","
            + "\"http://purl.org/dc/terms/title\":\"Tt1\","
            + "\"http://www.w3.org/2003/01/geo/wgs84_pos#longitude\":\"-131.3\"},"
            + "{\"http://purl.org/dc/terms/created\":\"not a date\"}]," // end v.e.M
            + "\"http://rs.gbif.org/terms/1.0/Image\":[" // verbatim.multimedia.Image
            + "{\"http://purl.org/dc/terms/license\":\"Lic1\","
            + "\"http://www.w3.org/2003/01/geo/wgs84_pos#latitude\":\"60.4\","
            + "\"http://purl.org/dc/terms/identifier\":\"http://www.gbif.org/tmp.jpg\","
            + "\"http://rs.tdwg.org/dwc/terms/datasetID\":\"1\","
            + "\"http://purl.org/dc/terms/description\":\"Desc1\","
            + "\"http://purl.org/dc/terms/publisher\":\"Pub1\","
            + "\"http://purl.org/dc/terms/audience\":\"Aud1\","
            + "\"http://purl.org/dc/terms/spatial\":\"Sp1\","
            + "\"http://purl.org/dc/terms/format\":\"jpeg\","
            + "\"http://purl.org/dc/terms/rightsHolder\":\"Rh1\","
            + "\"http://purl.org/dc/terms/creator\":\"Cr1\","
            + "\"http://purl.org/dc/terms/created\":\"2010\","
            + "\"http://purl.org/dc/terms/references\":\"http://www.gbif.org/tmp.jpg\","
            + "\"http://purl.org/dc/terms/contributor\":\"Cont1\","
            + "\"http://purl.org/dc/terms/title\":\"Tt1\","
            + "\"http://www.w3.org/2003/01/geo/wgs84_pos#longitude\":\"-131.3\"},"
            + "{\"http://purl.org/dc/terms/created\":\"not a date\"}]}}"; // end v.m.I, v.e, v
    assertEquals(expectedVerbatim, result.path("verbatim").toString());

    String expectedGbifClassification =
        "{\"usage\":{\"key\":2,\"name\":\"Name2\",\"rank\":\"ABERRATION\"},"
            + "\"classification\":[" // classification
            + "{\"key\":1,\"name\":\"Name\",\"rank\":\"CHEMOFORM\"},"
            + "{\"key\":2,\"name\":\"Name2\",\"rank\":\"ABERRATION\"}]," // end classification
            + "\"chemoformKey\":1,"
            + "\"chemoform\":\"Name\","
            + "\"aberrationKey\":2,"
            + "\"aberration\":\"Name2\","
            + "\"classificationPath\":\"_1\","
            + "\"taxonKey\":[1,2]}";
    assertEquals(expectedGbifClassification, result.path("gbifClassification").toString());
    assertEquals(
        "[{\"key\":\"data\",\"value\":\"value\"}]",
        result.path("locationFeatureLayers").toString());

    String expectedMeasurementOrFactItems =
        "[{\"id\":\"123\",\"type\":\"{\\\"something\\\":1}"
            + "{\\\"something\\\":1}\","
            + "\"value\":1.1,"
            + "\"determinedDate\":{\"gte\": \"2010\", \"lte\": \"2011\"}},{\"id\":\"124\","
            + "\"type\":null,"
            + "\"value\":null,"
            + "\"determinedDate\":{\"gte\": \"2010\", \"lte\": \"2012\"}}]";
    assertEquals(expectedMeasurementOrFactItems, result.path("measurementOrFactItems").toString());

    String expectedIssues = "[\"BASIS_OF_RECORD_INVALID\",\"ZERO_COORDINATE\"]";
    assertEquals(expectedIssues, result.path("issues").toString());
    assertEquals(
        OccurrenceIssue.values().length - expectedIssues.split(",").length,
        result.path("notIssues").size());
  }

  @Test
  public void onlyOneIdInJsonTest() {
    // State
    ExtendedRecord er = ExtendedRecord.newBuilder().setId("777").build();
    TemporalRecord tr = TemporalRecord.newBuilder().setId("777").build();

    // When
    ObjectNode result = GbifJsonConverter.toJson(er, tr);

    // Should
    assertTrue(JsonValidationUtils.isValid(result.toString()));
    assertEquals(er.getId(), result.path("id").asText());

    assertEquals(0, result.path("verbatim").path("core").size());
    assertEquals(0, result.path("verbatim").path("extensions").size());
    assertEquals(0, result.path("issues").size());
    assertEquals(OccurrenceIssue.values().length, result.path("notIssues").size());
  }

  @Test
  public void taxonRecordUsageTest() {

    // Expected
    String expected =
        "{"
            + "\"id\":\"777\","
            + "\"all\":[\"T1\",\"Name\"],"
            + "\"verbatim\":{\"core\":{\"http://rs.tdwg.org/dwc/terms/taxonID\":\"T1\","
            + "\"http://rs.tdwg.org/dwc/terms/scientificName\":\"Name\"},"
            + "\"extensions\":{}},"
            + "\"gbifClassification\":{\"taxonID\":\"T1\","
            + "\"verbatimScientificName\":\"Name\","
            + "\"usage\":{\"key\":1,"
            + "\"name\":\"n\","
            + "\"rank\":\"ABERRATION\"},"
            + "\"classification\":[{\"key\":1,"
            + "\"name\":\"Name\","
            + "\"rank\":\"CHEMOFORM\"},"
            + "{\"key\":2,"
            + "\"name\":\"Name2\","
            + "\"rank\":\"ABERRATION\"}]"
            + ","
            + "\"acceptedUsage\":{\"key\":2,"
            + "\"name\":\"Name2\","
            + "\"rank\":\"ABERRATION\"},"
            + "\"chemoformKey\":1,"
            + "\"chemoform\":\"Name\","
            + "\"aberrationKey\":2,"
            + "\"aberration\":\"Name2\","
            + "\"classificationPath\":\"_1\","
            + "\"taxonKey\":[1,2]},"
            + "\"created\":\"1970-01-01T00:00\"}";

    // State
    List<RankedName> rankedNameList = new ArrayList<>();
    RankedName name =
        RankedName.newBuilder().setKey(1).setName("Name").setRank(Rank.CHEMOFORM).build();
    RankedName name2 =
        RankedName.newBuilder().setKey(2).setName("Name2").setRank(Rank.ABERRATION).build();
    rankedNameList.add(name);
    rankedNameList.add(name2);

    TaxonRecord taxonRecord =
        TaxonRecord.newBuilder()
            .setId("777")
            .setCreated(0L)
            .setUsage(
                RankedName.newBuilder().setKey(1).setName("n").setRank(Rank.ABERRATION).build())
            .setClassification(rankedNameList)
            .setAcceptedUsage(name2)
            .build();

    ExtendedRecord extendedRecord =
        ExtendedRecord.newBuilder()
            .setId("777")
            .setCoreTerms(
                new ImmutableMap.Builder<String, String>()
                    .put(DwcTerm.taxonID.qualifiedName(), "T1")
                    .put(DwcTerm.scientificName.qualifiedName(), "Name")
                    .build())
            .build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(extendedRecord, taxonRecord);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void extendedRecordSkipIssuesWithIdTest() {

    // Expected
    String expected =
        "{"
            + "\"id\":\"777\","
            + "\"all\":[],"
            + "\"verbatim\":{\"core\":{},"
            + "\"extensions\":{}}}";

    // State
    ExtendedRecord record = ExtendedRecord.newBuilder().setId("777").build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void temporalRecordSkipIssuesWithIdTest() {

    // Expected
    String expected = "{\"id\":\"777\",\"created\":\"1970-01-01T00:00\"}";

    // State
    TemporalRecord record = TemporalRecord.newBuilder().setId("777").setCreated(0L).build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void locationRecordSkipIssuesWithIdTest() {

    // Expected
    String expected = "{\"id\":\"777\"}";

    // State
    LocationRecord record = LocationRecord.newBuilder().setId("777").build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void locationFeaturesRecordSkipIssuesWithIdTest() {

    // Expected
    String expected =
        "{"
            + "\"id\":\"777\","
            + "\"locationFeatureLayers\":[{\"key\":\"{awdawd}\","
            + "\"value\":\"\\\"{\\\"wad\\\":\\\"adw\\\"}\\\"\"}],"
            + "\"created\":\"1970-01-01T00:00\"}";

    // State
    LocationFeatureRecord record =
        LocationFeatureRecord.newBuilder()
            .setId("777")
            .setCreated(0L)
            .setItems(Collections.singletonMap("{awdawd}", "\"{\"wad\":\"adw\"}\""))
            .build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void measurementOrFactRecordSkipIssuesWithIdTest() {

    // Expected
    String expected =
        "{\"id\":\"777\"," + "\"measurementOrFactItems\":[]," + "\"created\":\"1970-01-01T00:00\"}";

    // State
    MeasurementOrFactRecord record =
        MeasurementOrFactRecord.newBuilder().setId("777").setCreated(0L).build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void amplificationRecordSkipIssuesWithIdEmptyTest() {

    // Expected
    String expected =
        "{\"id\":\"777\"," + "\"amplificationItems\":[]," + "\"created\":\"1970-01-01T00:00\"}";

    // State
    AmplificationRecord record =
        AmplificationRecord.newBuilder().setId("777").setCreated(0L).build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void multimediaRecordSkipIssuesWithIdTest() {

    // Expected
    String expected = "{\"id\":\"777\"}";

    // State
    MultimediaRecord record = MultimediaRecord.newBuilder().setId("777").build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void multimediaRecordSkipIssuesWithIdEmptyTest() {

    // Expected
    String expected =
        "{\"id\":\"777\","
            + "\"multimediaItems\":[{}],"
            + "\"mediaTypes\":[],"
            + "\"mediaLicenses\":[]}";

    // State
    MultimediaRecord record =
        MultimediaRecord.newBuilder()
            .setId("777")
            .setMultimediaItems(Collections.singletonList(Multimedia.newBuilder().build()))
            .build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void amplificationRecordSkipIssuesWithIdTest() {

    // Expected
    String expected =
        "{\"id\":\"777\","
            + "\"amplificationItems\":[{\"name\":\"n\","
            + "\"identity\":3,"
            + "\"appliedScientificName\":"
            + "\"sn\","
            + "\"matchType\":\"mt\","
            + "\"bitScore\":1,"
            + "\"expectValue\":2,"
            + "\"querySequence\":\"qs\","
            + "\"subjectSequence\":"
            + "\"ss\","
            + "\"qstart\":5,"
            + "\"qend\":4,"
            + "\"sstart\":8,"
            + "\"send\":6,"
            + "\"distanceToBestMatch\":\"dm\","
            + "\"sequenceLength\":7}]}";

    // State
    AmplificationRecord record =
        AmplificationRecord.newBuilder()
            .setId("777")
            .setAmplificationItems(
                Arrays.asList(
                    Amplification.newBuilder()
                        .setBlastResult(
                            BlastResult.newBuilder()
                                .setAppliedScientificName("sn")
                                .setBitScore(1)
                                .setDistanceToBestMatch("dm")
                                .setExpectValue(2)
                                .setIdentity(3)
                                .setMatchType("mt")
                                .setName("n")
                                .setQend(4)
                                .setQstart(5)
                                .setQuerySequence("qs")
                                .setSend(6)
                                .setSequenceLength(7)
                                .setSstart(8)
                                .setSubjectSequence("ss")
                                .build())
                        .build(),
                    Amplification.newBuilder().build()))
            .build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(record);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void multimediaRecordTest() {

    // Expected
    String expected =
        "{\"id\":\"777\","
            + "\"multimediaItems\":[{\"type\":\"StillImage\","
            + "\"format\":\"image/jpeg\","
            + "\"license\":\"somelicense\"},"
            + "{\"type\":\"MovingImage\","
            + "\"format\":\"video/mp4\","
            + "\"license\":\"somelicense\"}],"
            + "\"mediaTypes\":[\"StillImage\",\"MovingImage\"],"
            + "\"mediaLicenses\":[\"somelicense\"]}";

    // State
    Multimedia stillImage = new Multimedia();
    stillImage.setType(MediaType.StillImage.name());
    stillImage.setFormat("image/jpeg");
    stillImage.setLicense("somelicense");

    Multimedia movingImage = new Multimedia();
    movingImage.setType(MediaType.MovingImage.name());
    movingImage.setFormat("video/mp4");
    movingImage.setLicense("somelicense");

    MultimediaRecord multimediaRecord =
        MultimediaRecord.newBuilder()
            .setId("777")
            .setMultimediaItems(Arrays.asList(stillImage, movingImage))
            .build();

    // When
    String result = GbifJsonConverter.toStringPartialJson(multimediaRecord);

    // Should
    assertEquals(expected, result);
    assertTrue(JsonValidationUtils.isValid(result));
  }

  @Test
  public void emptyAvroWithIdTest() throws IOException {
    // State
    String k = "777";
    MetadataRecord mdr =
        MetadataRecord.newBuilder()
            .setId(k)
            .setDatasetKey("key")
            .setCrawlId(1)
            .setDatasetPublishingCountry("PC")
            .setLicense("l")
            .build();
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(k).build();
    BasicRecord br =
        BasicRecord.newBuilder().setId(k).setLicense(License.UNSPECIFIED.name()).build();
    TemporalRecord tr = TemporalRecord.newBuilder().setId(k).build();
    LocationRecord lr = LocationRecord.newBuilder().setId(k).build();
    TaxonRecord txr = TaxonRecord.newBuilder().setId(k).build();
    MultimediaRecord mr = MultimediaRecord.newBuilder().setId(k).build();
    ImageRecord ir = ImageRecord.newBuilder().setId(k).build();
    AudubonRecord ar = AudubonRecord.newBuilder().setId(k).build();
    MeasurementOrFactRecord mfr = MeasurementOrFactRecord.newBuilder().setId(k).build();

    // When
    MultimediaRecord mmr = MultimediaConverter.merge(mr, ir, ar);
    ObjectNode result = GbifJsonConverter.toJson(mdr, br, tr, lr, txr, mmr, mfr, er);

    // Should
    assertTrue(JsonValidationUtils.isValid(result.toString()));
    assertEquals(mdr.getId(), result.path("id").asText());
    assertEquals(mdr.getDatasetKey(), result.path("datasetKey").asText());
    assertEquals(mdr.getCrawlId(), (Integer) result.path("crawlId").asInt());
    assertEquals("l", result.path("license").asText());
    assertEquals(
        mdr.getDatasetPublishingCountry(), result.path("datasetPublishingCountry").asText());
    assertEquals(0, result.path("issues").size());
    assertEquals(0, result.path("gbifClassification").size());
    assertEquals(0, result.path("measurementOrFactItems").size());
    assertEquals(0, result.path("all").size());
    assertEquals(0, result.path("verbatim").path("core").size());
    assertEquals(0, result.path("verbatim").path("extensions").size());
    assertEquals(OccurrenceIssue.values().length, result.path("notIssues").size());
  }
}
