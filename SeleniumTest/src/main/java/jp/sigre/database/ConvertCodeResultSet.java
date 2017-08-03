package jp.sigre.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import jp.sigre.selenium.trade.TradeDataBean;

public class ConvertCodeResultSet implements ConvertResultSet {

	@Override
	public TradeDataBean rsToBean(ResultSet rs) throws SQLException {
		TradeDataBean info = new TradeDataBean();

		info.setCode				(rs.getString("code"));
		info.setRealEntryVolume(rs.getString("realEntryVolume"));

		return info;
	}

}
