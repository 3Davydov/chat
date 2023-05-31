package com.daniel.XMLConverter.ServerToXML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.daniel.XMLConverter.Converter;
import com.daniel.ctsmessages.CTSMessage;
import com.daniel.exceptions.ConvertionException;
import com.daniel.stcmessages.STCMessage;
import com.daniel.stcmessages.ServerKeepAlive;

public class ServerKeepaliveMessageConverter extends Converter {

    private String pathToTemplate = "src/main/XMLTemplates/keepalive/serverMessage.xml";

    @Override
    public String convertToSerializableXML(ArrayList<Object> params) throws ConvertionException {
        File xmlFile = new File(pathToTemplate);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ConvertionException(e.getMessage());
        }
        return serializeDocument(document);
    }

    @Override
    public STCMessage convertFromSerializableXMLtoSM(Document serializedXML) {
        STCMessage message = new ServerKeepAlive();
        return message;
    }

    @Override
    public CTSMessage convertFromSerializableXMLtoCM(Document serializedXML) {
        throw new UnsupportedOperationException("Unsupported conversion");
    }
}
