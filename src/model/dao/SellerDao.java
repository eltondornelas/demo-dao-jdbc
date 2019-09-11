package model.dao;

import java.util.List;

import model.entities.Department;
import model.entities.Seller;

public interface SellerDao {

	void insert(Seller obj);
	void update(Seller obj);
	void deleteById(Integer id);
	Seller findById(Integer id); //returna um department, pega o id e consulta um obj com esse id, se não existir retorna nulo
	List<Seller> findAll();
	List<Seller> findByDepartment(Department department);
}
