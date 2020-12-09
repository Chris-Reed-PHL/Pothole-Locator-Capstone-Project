package com.techelevator.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.dao.PotholeDAO;
import com.techelevator.model.Pothole;
import com.techelevator.model.PotholeDTO;

@RestController
@CrossOrigin
public class PotholeController {
	
	private PotholeDAO potholeDAO;

	public PotholeController(PotholeDAO potholeDAO) {
		this.potholeDAO = potholeDAO;
	}
	
	//Get all potholes
	@RequestMapping(value = "/potholes", method = RequestMethod.GET)
	public List<Pothole> listAllPotholes() {
		return potholeDAO.findAllPotholes();
	}
	
	//Get pothole by ID
	@RequestMapping(value = "/potholes/{id}", method = RequestMethod.GET)
	public Pothole getPothole(@PathVariable int id) {
		return potholeDAO.getPothole(id);
	}
	
	//Get pothole by Status
	@RequestMapping(value = "/potholes/status/{statusId}", method = RequestMethod.GET)
	public List<Pothole> getPotholeByStatus(@PathVariable int statusId){
		return potholeDAO.getPotholeByStatus(statusId);
	}
	
	//Add pothole 
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/potholes", method = RequestMethod.POST)
	public void addPothole(@Valid @RequestBody PotholeDTO newPothole) {
		potholeDAO.createPothole(newPothole);
	}
	
	
	//Update pothole
	@RequestMapping(value = "/potholes/{potholeId}", method = RequestMethod.PUT)
	public void updatePothole(@Valid @RequestBody PotholeDTO updatedPothole, @PathVariable int potholeId) {
		potholeDAO.updatePothole(updatedPothole, potholeId);
	}
	
	//Update pothole severity
	@RequestMapping(value = "/potholes/{potholeId}/severity", method = RequestMethod.PUT)
	public void updatePotholeSeverity(@Valid @RequestBody PotholeDTO updatedPothole, @PathVariable int potholeId) {
		potholeDAO.updatePotholeSeverity(potholeId,updatedPothole.getSeverity());
	}
	
	//Update pothole status
	@RequestMapping(value = "potholes/{potholeId}/status", method = RequestMethod.PUT)
	public void updatePotholeStatus(@Valid @RequestBody PotholeDTO updatedPothole, @PathVariable int potholeId) {
		potholeDAO.updatePotholeStatus(potholeId, updatedPothole.getStatus());
	}
	
	//Delete pothole Might not use may just changed status to deleted
//	@ResponseStatus(HttpStatus.OK)
//	@RequestMapping(value = "/potholes/{id}", method = RequestMethod.DELETE)
//	public void deletelPothole(@PathVariable int id) {
//		potholeDAO.deletePothole(id);
//	}
}
