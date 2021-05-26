import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
public class QmsMaterielRepositoryImpl implements QmsMaterielRepository{

 private RestTemplate restTemplate;

  String url = "http://2";


public Optional<QmsMateriel> findQmsMaterielByMaterielCdAndFlagStatus(String materielCd,String flagStatus){
  UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url.concat("/findQmsMaterielByMaterielCdAndFlagStatus"))
    .queryParam("materielCd",materielCd)
    .queryParam("flagStatus",flagStatus);
  Optional<QmsMateriel> aux = restTemplate.getForObject(builder.toUriString(), Optional<QmsMateriel>.class)

 return aux;
}


}