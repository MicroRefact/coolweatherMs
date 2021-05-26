import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
public class QmsProcessRepositoryImpl implements QmsProcessRepository{

 private RestTemplate restTemplate;

  String url = "http://14";


public QmsProcess findByFlagStatusAndId(String string,Long valueOf){
  UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url.concat("/findByFlagStatusAndId"))
    .queryParam("string",string)
    .queryParam("valueOf",valueOf);
  QmsProcess aux = restTemplate.getForObject(builder.toUriString(), QmsProcess.class)

 return aux;
}


}