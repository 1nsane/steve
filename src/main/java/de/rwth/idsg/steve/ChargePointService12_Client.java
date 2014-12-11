package de.rwth.idsg.steve;

import de.rwth.idsg.steve.common.utils.DateTimeUtils;
import de.rwth.idsg.steve.html.InputException;
import ocpp.cp._2010._08.*;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client implementation of OCPP V1.2.
 * 
 * This class has methods to create request payloads, and methods to send these to charge points from dynamically created clients.
 * Since there are multiple charge points and their endpoint addresses vary, the clients need to be created dynamically.
 * 
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * 
 */
public class ChargePointService12_Client {

    private static final Logger LOG = LoggerFactory.getLogger(ChargePointService12_Client.class);
    private static JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

    static {
        factory.setBindingId("http://schemas.xmlsoap.org/wsdl/soap12/");
        factory.getFeatures().add(new WSAddressingFeature());
        factory.setServiceClass(ChargePointService.class);
    }

    /////// CREATE Request Payloads ///////

    public ChangeAvailabilityRequest prepareChangeAvailability(int connectorId, String availTypeStr){
        ChangeAvailabilityRequest req = new ChangeAvailabilityRequest();
        req.setConnectorId(connectorId);
        req.setType( AvailabilityType.fromValue(availTypeStr) );
        return req;
    }

    public ChangeConfigurationRequest prepareChangeConfiguration(String confKey, String value){
        ChangeConfigurationRequest req = new ChangeConfigurationRequest();
        req.setKey(confKey);
        req.setValue(value);
        return req;
    }

    public ClearCacheRequest prepareClearCache(){
        return new ClearCacheRequest();
    }

    public GetDiagnosticsRequest prepareGetDiagnostics(String location, int retries, int retryInterval, String startTime, String stopTime){
        GetDiagnosticsRequest req = new GetDiagnosticsRequest();
        req.setLocation(location);
        if (retries != -1) req.setRetries(retries);
        if (retryInterval != -1) req.setRetryInterval(retryInterval);

        //// Act according to four boolean combinations of the two variables ////

        if (startTime == null && stopTime == null) {
            // Do not set the fields

        } else if (startTime == null) {
            DateTime stop = DateTimeUtils.convertToDateTime(stopTime);
            if (stop.isBeforeNow()) {
                req.setStopTime(DateTimeUtils.convertToXMLGregCal(stop));
            } else {
                throw new InputException("Stop date/time must be in the past.");
            }

        } else if (stopTime == null) {
            DateTime start = DateTimeUtils.convertToDateTime(startTime);
            if (start.isBeforeNow()) {
                req.setStartTime( DateTimeUtils.convertToXMLGregCal(start) );
            } else {
                throw new InputException("Start date/time must be in the past.");
            }

        } else {
            DateTime start = DateTimeUtils.convertToDateTime(startTime);
            DateTime stop = DateTimeUtils.convertToDateTime(stopTime);
            if (stop.isBeforeNow() && start.isBefore(stop)) {
                req.setStartTime( DateTimeUtils.convertToXMLGregCal(start) );
                req.setStopTime( DateTimeUtils.convertToXMLGregCal(stop) );
            } else {
                throw new InputException("Start date/time must be before the stop date/time, and both must be in the past.");
            }
        }

        return req;
    }

    public RemoteStartTransactionRequest prepareRemoteStartTransaction(int connectorId, String idTag){
        RemoteStartTransactionRequest req = new RemoteStartTransactionRequest();
        if (connectorId != 0) req.setConnectorId(connectorId);
        req.setIdTag(idTag);
        return req;
    }

    public RemoteStopTransactionRequest prepareRemoteStopTransaction(int transactionId){
        RemoteStopTransactionRequest req = new RemoteStopTransactionRequest();
        req.setTransactionId(transactionId);
        return req;
    }

    public ResetRequest prepareReset(String resetTypeStr){
        ResetRequest req = new ResetRequest();
        req.setType( ResetType.fromValue(resetTypeStr) );
        return req;
    }

    public UnlockConnectorRequest prepareUnlockConnector(int connectorId){
        UnlockConnectorRequest req = new UnlockConnectorRequest();
        req.setConnectorId(connectorId);
        return req;
    }

    public UpdateFirmwareRequest prepareUpdateFirmware(String location, int retries, String retrieveDate, int retryInterval){
        UpdateFirmwareRequest req = new UpdateFirmwareRequest();
        req.setLocation(location);
        req.setRetrieveDate( DateTimeUtils.convertToXMLGregCal(retrieveDate) );
        if (retries != -1) req.setRetries(retries);
        if (retryInterval != -1) req.setRetryInterval(retryInterval);
        return req;
    }

    /////// SEND Request Payloads ///////

    public String sendChangeAvailability(String chargeBoxId, String endpoint_address, ChangeAvailabilityRequest req){
        LOG.info("Invoking changeAvailability at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        ChangeAvailabilityResponse response = client.changeAvailability(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: ChangeAvailability, Response: " + response.getStatus().value();
    }

    public String sendChangeConfiguration(String chargeBoxId, String endpoint_address, ChangeConfigurationRequest req){
        LOG.info("Invoking changeConfiguration at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        ChangeConfigurationResponse response = client.changeConfiguration(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: ChangeConfiguration, Response: " + response.getStatus().value();
    }

    public String sendClearCache(String chargeBoxId, String endpoint_address, ClearCacheRequest req){
        LOG.info("Invoking clearCache at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        ClearCacheResponse response = client.clearCache(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: ClearCache, Response: " + response.getStatus().value();
    }

    public String sendGetDiagnostics(String chargeBoxId, String endpoint_address, GetDiagnosticsRequest req){
        LOG.info("Invoking getDiagnostics at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        GetDiagnosticsResponse response = client.getDiagnostics(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: GetDiagnostics, Response: " + response.getFileName();
    }

    public String sendRemoteStartTransaction(String chargeBoxId, String endpoint_address, RemoteStartTransactionRequest req){
        LOG.info("Invoking remoteStartTransaction at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        RemoteStartTransactionResponse response = client.remoteStartTransaction(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: RemoteStartTransaction, Response: " + response.getStatus().value();
    }

    public String sendRemoteStopTransaction(String chargeBoxId, String endpoint_address, RemoteStopTransactionRequest req){
        LOG.info("Invoking remoteStopTransaction at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        RemoteStopTransactionResponse response = client.remoteStopTransaction(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: RemoteStopTransaction, Response: " + response.getStatus().value();
    }

    public String sendReset(String chargeBoxId, String endpoint_address, ResetRequest req){
        LOG.info("Invoking reset at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        ResetResponse response = client.reset(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: Reset, Response: " + response.getStatus().value();
    }

    public String sendUnlockConnector(String chargeBoxId, String endpoint_address, UnlockConnectorRequest req){
        LOG.info("Invoking unlockConnector at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        UnlockConnectorResponse response = client.unlockConnector(req, chargeBoxId);
        return "Charge point: " + chargeBoxId + ", Request: UnlockConnector, Response: " + response.getStatus().value();
    }

    public String sendUpdateFirmware(String chargeBoxId, String endpoint_address, UpdateFirmwareRequest req){
        LOG.info("Invoking updateFirmware at {}", chargeBoxId);
        factory.setAddress(endpoint_address);
        ChargePointService client = (ChargePointService) factory.create();
        UpdateFirmwareResponse response = client.updateFirmware(req, chargeBoxId);
        String str = "";
        if (response != null) str = "OK";
        return "Charge point: " + chargeBoxId + ", Request: UpdateFirmware, Response: " + str;
    }
}