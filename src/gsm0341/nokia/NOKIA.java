package gsm0341.nokia;

import gsm0341.nokia.generated.CBSEapdus;
import gsm0341.nokia.generated.CellId;
import gsm0341.nokia.generated.CellIdListDisc;
import gsm0341.nokia.generated.CellIdListDiscBase;
import gsm0341.nokia.generated.CellList;
import gsm0341.nokia.generated.CellList.CellListSequenceType;
import gsm0341.nokia.generated.Reset;
import gsm0341.nokia.generated.StatusCBCHQuery;
import gsm0341.util.CharsetTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;
import org.bn.annotations.ASN1Module;

@ASN1Module(name = "NOKIA", isImplicitTags = true)
public class NOKIA {

	public static final byte[] lacAndCi = { (byte) 0x01 };
	public static final byte[] ciOnly = { (byte) 0x02 };
	public static final byte[] allCellsOnBSS = { (byte) 0x06 };

	public static byte[] encodeReset(List<byte[]> cellIds) throws Exception {
		CellIdListDiscBase cellIdListDiscBase = new CellIdListDiscBase();
		cellIdListDiscBase.setValue(lacAndCi);

		CellIdListDisc cellIdListDisc = new CellIdListDisc();
		cellIdListDisc.setValue(cellIdListDiscBase);

		List<CellListSequenceType> cellListSequenceTypes = new ArrayList<CellListSequenceType>();

		for (byte[] cell : cellIds) {
			CellId cellId = new CellId();
			cellId.setValue(cell);

			CellListSequenceType cellListSequenceType = new CellListSequenceType();
			cellListSequenceType.setId(cellId);

			cellListSequenceTypes.add(cellListSequenceType);
		}

		CellList cellList = new CellList();
		cellList.setDisc(cellIdListDisc);
		cellList.setCellList(cellListSequenceTypes);

		Reset reset = new Reset();
		reset.setCellList(cellList);

		CBSEapdus cbseApdus = new CBSEapdus();
		cbseApdus.selectCbseReset(reset);

		IEncoder<CBSEapdus> encoder = CoderFactory.getInstance().newEncoder("BER");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encoder.encode(cbseApdus, outputStream);

		return outputStream.toByteArray();
	}

	public static byte[] encodeStatusCBCHQuery(List<byte[]> cellIds) throws Exception {
		CellIdListDiscBase cellIdListDiscBase = new CellIdListDiscBase();
		cellIdListDiscBase.setValue(lacAndCi);

		CellIdListDisc cellIdListDisc = new CellIdListDisc();
		cellIdListDisc.setValue(cellIdListDiscBase);

		List<CellListSequenceType> cellListSequenceTypes = new ArrayList<CellListSequenceType>();

		for (byte[] cell : cellIds) {
			CellId cellId = new CellId();
			cellId.setValue(cell);

			CellListSequenceType cellListSequenceType = new CellListSequenceType();
			cellListSequenceType.setId(cellId);

			cellListSequenceTypes.add(cellListSequenceType);
		}

		CellList cellList = new CellList();
		cellList.setDisc(cellIdListDisc);
		cellList.setCellList(cellListSequenceTypes);

		StatusCBCHQuery statusCBCHQuery = new StatusCBCHQuery();
		statusCBCHQuery.setCellList(cellList);

		CBSEapdus cbseApdus = new CBSEapdus();
		cbseApdus.selectCbseStatusCBCHQuery(statusCBCHQuery);

		IEncoder<CBSEapdus> encoder = CoderFactory.getInstance().newEncoder("BER");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encoder.encode(cbseApdus, outputStream);

		return outputStream.toByteArray();
	}

	public static boolean isStatusCBCHQueryResp(byte[] data) {
		try {
			CBSEapdus cbseAPDUs = decodeCBSEapdus(data);
			return cbseAPDUs.isCbseStatusCBCHQueryRespSelected();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isRestartIndication(byte[] data) {
		try {
			CBSEapdus cbseAPDUs = decodeCBSEapdus(data);
			return cbseAPDUs.isCbseRestartIndicationSelected();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isFailureIndication(byte[] data) {
		try {
			CBSEapdus cbseAPDUs = decodeCBSEapdus(data);
			return cbseAPDUs.isCbseFailureIndicationSelected();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isReject(byte[] data) {
		try {
			CBSEapdus cbseAPDUs = decodeCBSEapdus(data);
			return cbseAPDUs.isCbseRejectSelected();
		} catch (Exception e) {
			return false;
		}
	}

	private static CBSEapdus decodeCBSEapdus(byte[] data) throws Exception {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

		IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
		CBSEapdus cbseAPDUs = decoder.decode(inputStream, CBSEapdus.class);

		return cbseAPDUs;
	}

	public static void main(String[] args) throws Exception {
		// init data
		List<byte[]> cellIds = new ArrayList<byte[]>();

		byte[] cellId = new byte[4];
		int lac = 20170;
		int ci = 56093;
		cellId[0] = (byte) ((lac & 0xFF00) >> 8);
		cellId[1] = (byte) (lac & 0x00FF);
		cellId[2] = (byte) ((ci & 0xFF00) >> 8);
		cellId[3] = (byte) (ci & 0x00FF);
		cellIds.add(cellId);

		// test encode cbch query
		byte[] cbchQuery = encodeStatusCBCHQuery(cellIds);
		CharsetTools.printHex(cbchQuery);
		System.out.println("--StatusCBCHQuery");

		// test encode reset
		byte[] reset = encodeReset(cellIds);
		CharsetTools.printHex(reset);
		System.out.println("--Reset");

		// test reject check
		String rejectMessage = "A8 80 80 01 02 00 00";
		byte[] reject = CharsetTools.hexStringToByteArray(rejectMessage);
		System.out.println(rejectMessage);
		System.out.println(isReject(reject) ? "--Reject" : "--not Reject");

		// test StatusCBCHQueryResp check
		String cbchQueryResp = "A5 80 A0 80 30 80 A0 80 80 01 01 81 04 4E CA DA CB 00 00 81 01 03 00 00 00 00 82 01 00 00 00";
		byte[] statusCBCHQueryResp = CharsetTools.hexStringToByteArray(cbchQueryResp);
		System.out.println(cbchQueryResp);
		System.out.println(isStatusCBCHQueryResp(statusCBCHQueryResp) ? "--StatusCBCHQueryResp" : "--not StatusCBCHQueryResp");
	}
}
