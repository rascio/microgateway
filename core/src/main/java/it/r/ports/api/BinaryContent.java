package it.r.ports.api;


import lombok.Value;

@Value
public class BinaryContent {

	protected byte[] data;
    protected String contentType;

	@Override
    public String toString() {
        return "BinaryContent {" +
            "data=" + data.length +
            ", contentType='" + contentType + '\'' +
            '}';
    }
    
}
