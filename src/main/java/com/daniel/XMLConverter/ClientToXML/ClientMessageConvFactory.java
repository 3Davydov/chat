package com.daniel.XMLConverter.ClientToXML;

import java.util.ArrayList;

import com.daniel.XMLConverter.Converter;
import com.daniel.XMLConverter.ConverterFactory;
import com.daniel.exceptions.ConvertionException;

public class ClientMessageConvFactory extends ConverterFactory {

    @Override
    protected Converter createConverter(String messageName, ArrayList<Object> params) {
        if (messageName.equals("login")) return new LoginMessageConverter();
        if (messageName.equals("logout")) return new LogoutMessageConverter();
        if (messageName.equals("text")) return new ChatMessageConverter();
        if (messageName.equals("list")) return new ListMessageConverter();
        return null;
    }

    @Override
    protected Converter createConverter(String serializedXML) throws ConvertionException {
        String name = Converter.deserializeDocument(serializedXML).getDocumentElement().getAttribute("name");
        if (name.equals("login")) return new LoginMessageConverter();
        if (name.equals("logout")) return new LogoutMessageConverter();
        if (name.equals("text")) return new ChatMessageConverter();
        if (name.equals("list")) return new ListMessageConverter();
        return null;
    }
}
