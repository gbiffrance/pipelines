package au.org.ala.pipelines.interpreters;

import static org.gbif.pipelines.core.utils.ModelUtils.*;

import au.org.ala.pipelines.parser.CoordinatesParser;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.kvs.geocode.LatLng;
import org.gbif.pipelines.core.parsers.common.ParsedField;
import org.gbif.pipelines.io.avro.DistributionOutlierRecord;
import org.gbif.pipelines.io.avro.ExtendedRecord;
import org.gbif.pipelines.io.avro.IndexRecord;

/*
 * living atlases.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DistributionOutlierInterpreter {

  public static void interpretOccurrenceID(IndexRecord ir, DistributionOutlierRecord dr) {
    dr.setId(ir.getId());
  }

  public static void interpretLocation(IndexRecord ir, DistributionOutlierRecord dr) {
    String latlng = ir.getLatLng();
    String[] coordinates = latlng.split(",");
    dr.setDecimalLatitude(Double.parseDouble(coordinates[0]));
    dr.setDecimalLongitude(Double.parseDouble(coordinates[1]));
  }

  public static void interpretSpeciesId(IndexRecord ir, DistributionOutlierRecord dr) {
    dr.setSpeciesID(ir.getTaxonID());
  }

  /*
   * Interprete from verbatim
   */
  public static void interpretOccurrenceID(ExtendedRecord er, DistributionOutlierRecord dr) {
    String value = extractNullAwareValue(er, DwcTerm.occurrenceID);
    if (!Strings.isNullOrEmpty(value)) {
      dr.setId(value);
    }
  }

  public static void interpretLocation(ExtendedRecord er, DistributionOutlierRecord dr) {
    ParsedField<LatLng> parsedLatLon = CoordinatesParser.parseCoords(er);
    addIssue(dr, parsedLatLon.getIssues());

    if (parsedLatLon.isSuccessful()) {
      LatLng latlng = parsedLatLon.getResult();
      dr.setDecimalLatitude(latlng.getLatitude());
      dr.setDecimalLongitude(latlng.getLongitude());
    }
  }

  public static void interpretSpeciesId(ExtendedRecord er, DistributionOutlierRecord dr) {
    String value = extractNullAwareValue(er, DwcTerm.taxonConceptID);
    if (!Strings.isNullOrEmpty(value)) {
      dr.setSpeciesID(value);
    }
  }
}
