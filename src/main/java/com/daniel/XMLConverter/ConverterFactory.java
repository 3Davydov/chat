package com.daniel.XMLConverter;

import java.util.ArrayList;

import com.daniel.ctsmessages.CTSMessage;
import com.daniel.exceptions.ConvertionException;
import com.daniel.stcmessages.STCMessage;

public abstract class ConverterFactory {

    public String convertToSerializableXML(String messageName, ArrayList<Object> params) throws ConvertionException {
        Converter converter = createConverter(messageName, params);
        return converter.convertToSerializableXML(params);
    }

    public CTSMessage convertFromSerializableXMLtoCM(String serializedXML) throws ConvertionException {
        Converter converter = createConverter(serializedXML);
        return converter.convertFromSerializableXMLtoCM(Converter.deserializeDocument(serializedXML));
    }

    public STCMessage convertFromSerializableXMLtoSM(String serializedXML) throws ConvertionException {
        Converter converter = createConverter(serializedXML);
        return converter.convertFromSerializableXMLtoSM(Converter.deserializeDocument(serializedXML));
    }

    protected abstract Converter createConverter(String messageName, ArrayList<Object> params);
    protected abstract Converter createConverter(String serializedXML) throws ConvertionException;
}
