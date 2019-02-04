import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.knowm.xchange.oer.dto.marketdata.OERTickers;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public interface OERInterface {

  @GET
  @Path("latest.json")
  OERTickers getTickers(
      @QueryParam("app_id") String appId)
      throws IOException;
}
