package com.daniel.XMLConverter.ServerToXML;

import java.util.ArrayList;

import com.daniel.XMLConverter.Converter;
import com.daniel.XMLConverter.ConverterFactory;
import com.daniel.exceptions.ConvertionException;

public class ServerMessageConvFactory extends ConverterFactory {
    
    @Override
    protected Converter createConverter(String messageName, ArrayList<Object> params) {
        if (messageName.equals("LoginStatus")) return new LoginStatusConverter();
        if (messageName.equals("chatHistory")) return new ChatHistoryConverter();
        if (messageName.equals("error")) return new ErrorMessageConverter();
        if (messageName.equals("filledList")) return new ParticipantsListConverter();
        return null;
    }

    @Override
    protected Converter createConverter(String serializedXML) throws ConvertionException {
        String name = Converter.deserializeDocument(serializedXML).getDocumentElement().getAttribute("name");
        if (name.equals("LoginStatus")) return new LoginStatusConverter();
        if (name.equals("chatHistory")) return new ChatHistoryConverter();
        if (name.equals("error")) return new ErrorMessageConverter();
        if (name.equals("filledList")) return new ParticipantsListConverter();
        return null;
    }
}
