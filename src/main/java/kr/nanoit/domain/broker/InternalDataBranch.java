package kr.nanoit.domain.broker;

import kr.nanoit.domain.payload.Payload;
import kr.nanoit.domain.payload.PayloadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalDataBranch implements InternalData {

    private MetaData metaData;
    private Payload payload;


    @Override
    public String UUID() {
        return metaData.getSocketUuid();
    }
}
