package com.techelevator.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.techelevator.model.ClaimDTO;

@Service
public class ClaimSqlDAO implements ClaimDAO {

	private JdbcTemplate jdbcTemplate;

	public ClaimSqlDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<ClaimDTO> getAllClaims() {

		List<ClaimDTO> allClaims = new ArrayList<>();

		String gettingClaims = "SELECT c.claim_id, c.amount, c.description, cs.status, u.username "
				+ "FROM claims c INNER JOIN claim_status cs ON c.claim_status_id = cs.claim_status_id "
				+ "INNER JOIN users_claims uc ON c.claim_id = uc.claim_id "
				+ "INNER JOIN users u ON uc.user_id = u.user_id ;";

		SqlRowSet result = jdbcTemplate.queryForRowSet(gettingClaims);

		while (result.next()) {
			ClaimDTO claim = mapToClaim(result);
			allClaims.add(claim);
		}

		return allClaims;
	}

	@Override
	public ClaimDTO getClaimById(Long claimId) {

		ClaimDTO claim = null;

		String claimById = "SELECT c.claim_id, c.amount, c.description, cs.status, u.username "
				+ "FROM claims c INNER JOIN claim_status cs ON c.claim_status_id = cs.claim_status_id "
				+ "INNER JOIN users_claims uc ON c.claim_id = uc.claim_id "
				+ "INNER JOIN users u ON uc.user_id = u.user_id " + "WHERE c.claim_id = ?;";

		SqlRowSet result = jdbcTemplate.queryForRowSet(claimById, claimId);

		while (result.next()) {
			claim = mapToClaim(result);
		}
		return claim;
	}

	@Override
	public List<ClaimDTO> getUsersClaim(String username) {

		List<ClaimDTO> userClaims = new ArrayList<>();

		String gettingClaimsByUser = "SELECT c.claim_id, c.amount, c.description, cs.status, u.username "
				+ "FROM claims c INNER JOIN claim_status cs ON c.claim_status_id = cs.claim_status_id "
				+ "INNER JOIN users_claims uc ON c.claim_id = uc.claim_id "
				+ "INNER JOIN users u ON uc.user_id = u.user_id " + "WHERE username = ?;";

		SqlRowSet result = jdbcTemplate.queryForRowSet(gettingClaimsByUser, username);

		while (result.next()) {
			ClaimDTO claim = mapToClaim(result);
			userClaims.add(claim);
		}

		return userClaims;
	}

	@Override
	public boolean createClaim(ClaimDTO newClaim) {

		boolean claim = false;
		
	
		
		newClaim.setStatus("Submitted");
		// add to claims
		String createNewClaim = "INSERT INTO claims(claim_id, amount, description, claim_status_id) "
				+ "VALUES(DEFAULT, ?, ?,(SELECT claim_status_id FROM claim_status WHERE status=?));";

		int result = jdbcTemplate.update(createNewClaim, newClaim.getClaimAmount(), newClaim.getDescription(),
				newClaim.getStatus());

		if (result == 1) {
			// add to users_claims
			Long newClaimId = getClaimId(newClaim);
			newClaim.setClaimId(newClaimId);

			if (addToUsersClaims(newClaim)) {
				// add to potholes_claims
				if (addToPotholeClaims(newClaim)) {
					// add to claims_history
					if (addToClaimsHistory(newClaim)) {

						// add to user_claims_history
						if (addToUsersClaimsHistory(newClaim)) {

							claim = true;
						}
					}
				}
			}
		}
		return claim;
	}

	// able to update amount and status
	@Override
	public boolean updateClaim(ClaimDTO updatedClaim, int claimId) {
		boolean claim = false;

		String updatesClaims = "UPDATE claims SET amount = ? , claim_status_id =(SELECT claim_status_id FROM claim_status WHERE status=?) "
				+ " WHERE claim_id = ?;";

		if (addToClaimsHistory(updatedClaim)) {
			int result = jdbcTemplate.update(updatesClaims, updatedClaim.getClaimAmount(), updatedClaim.getStatus(),
					claimId);

			if (result == 1) {
				claim = true;
			}
		}
		return claim;

	}

	private boolean addToClaimsHistory(ClaimDTO claimHistory) {

		boolean claim = false;

		String addToClaimHistory = "INSERT INTO claims_history "
				+ "(claim_history_id, claim_id, amount, description, claim_status_id, datetime) "
				+ "VALUES(DEFAULT, ?, ?, ?,(SELECT claim_status_id FROM claim_status WHERE status=?), CURRENT_TIMESTAMP);";

		int result = jdbcTemplate.update(addToClaimHistory, claimHistory.getClaimId(), claimHistory.getClaimAmount(),
				claimHistory.getDescription(), claimHistory.getStatus());

		if (result == 1) {
			claim = true;
		}
		return claim;
	}

	private boolean addToUsersClaims(ClaimDTO newClaim) {

		boolean claim = false;

		String addToUsersClaims = "INSERT INTO users_claims(user_id, claim_id) "
				+ "VALUES((SELECT user_id FROM users WHERE username = ?), ?);";
		int result = jdbcTemplate.update(addToUsersClaims, newClaim.getUsername(), newClaim.getClaimId());

		if (result == 1) {
			claim = true;
		}
		return claim;

	}

	private boolean addToPotholeClaims(ClaimDTO newClaim) {

		boolean claim = false;

		String addToPotholeClaims = "INSERT INTO potholes_claims(pothole_id, claim_id) " + "VALUES(?, ?);";

		int result = jdbcTemplate.update(addToPotholeClaims, newClaim.getPotholeId(), newClaim.getClaimId());

		if (result == 1) {
			claim = true;
		}
		return claim;

	}

	private boolean addToUsersClaimsHistory(ClaimDTO claimWithUser) {

		boolean claim = false;

		String addToUsersClaimsHistory = "INSERT INTO users_claims_history(user_id, claim_history_id) "
				+ "VALUES((SELECT user_id FROM users WHERE username = ?), "
				+ "(SELECT claim_history_id FROM claims_history WHERE claim_id = ?));";

		int result = jdbcTemplate.update(addToUsersClaimsHistory, claimWithUser.getUsername(),
				claimWithUser.getClaimId());
		if (result == 1) {
			claim = true;
		}
		return claim;
	}

	public Long getClaimId(ClaimDTO claim) {

		String getClaimId = "SELECT claim_id "
				+ "FROM claims "
				+ "WHERE amount = ? and description = ?;";
		return jdbcTemplate.queryForObject(getClaimId, long.class, claim.getClaimAmount(), claim.getDescription());

	}

	// map to claim with username
	private ClaimDTO mapToClaim(SqlRowSet cl) {

		ClaimDTO claims = new ClaimDTO();

		claims.setClaimId(cl.getLong("claim_id"));
		claims.setClaimAmount(cl.getFloat("amount"));
		claims.setStatus(cl.getString("status"));
		claims.setDescription(cl.getString("description"));
		claims.setUsername(cl.getString("username"));

		return claims;

	}

}
