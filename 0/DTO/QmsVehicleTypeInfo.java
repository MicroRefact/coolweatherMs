import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence;
import javax.validation.constraints;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
public class QmsVehicleTypeInfo implements Serializable{

 private  long serialVersionUID;

 private  Long id;

 private  Integer vehicleClassId;

 private  String vehicleType;

 private  String vehicleTypeName;

 private  String remark;

 private  String flagStatus;

 private  String compPkid;

 private  String reserveFirst;

 private  String reserveSecond;

 private  String reserveThird;

 private  String makeUser;

 private  ZonedDateTime makeTime;

 private  String modifyUser;

 private  ZonedDateTime modifyTime;

 private RestTemplate restTemplate = new RestTemplate();

  String url = "http://1";


public Long getId(){
    return id;
}


public String getMakeUser(){
    return makeUser;
}


public String getReserveThird(){
    return reserveThird;
}


public ZonedDateTime getMakeTime(){
    return makeTime;
}


public String getRemark(){
    return remark;
}


public String getReserveFirst(){
    return reserveFirst;
}


public String getReserveSecond(){
    return reserveSecond;
}


public ZonedDateTime getModifyTime(){
    return modifyTime;
}


public String getCompPkid(){
    return compPkid;
}


public String getVehicleTypeName(){
    return vehicleTypeName;
}


public String getModifyUser(){
    return modifyUser;
}


public Integer getVehicleClassId(){
    return vehicleClassId;
}


public String getFlagStatus(){
    return flagStatus;
}


public String getVehicleType(){
    return vehicleType;
}


@Override
public String toString(){
    return "QmsVehicleTypeInfo{" + "id=" + getId() + ", vehicleClassId=" + getVehicleClassId() + ", vehicleType='" + getVehicleType() + "'" + ", vehicleTypeName='" + getVehicleTypeName() + "'" + ", remark='" + getRemark() + "'" + ", flagStatus='" + getFlagStatus() + "'" + ", compPkid='" + getCompPkid() + "'" + ", reserveFirst='" + getReserveFirst() + "'" + ", reserveSecond='" + getReserveSecond() + "'" + ", reserveThird='" + getReserveThird() + "'" + ", makeUser='" + getMakeUser() + "'" + ", makeTime='" + getMakeTime() + "'" + ", modifyUser='" + getModifyUser() + "'" + ", modifyTime='" + getModifyTime() + "'" + "}";
 

  UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url.concat("/"+ id).concat("/toString"));

String aux = restTemplate.getForObject(builder.toUriString(),String.class);
return aux;
}


}