import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
public class RbacRoleRepositoryImpl implements RbacRoleRepository{

 private RestTemplate restTemplate;

  String url = "http://10";


public List<RbacRole> findByRoleCode(String s){
  UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url.concat("/findByRoleCode"))
    .queryParam("s",s);
  List<RbacRole> aux = restTemplate.getForObject(builder.toUriString(), List<RbacRole>.class)

 return aux;
}


}