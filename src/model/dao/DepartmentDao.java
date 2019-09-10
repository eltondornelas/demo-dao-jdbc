package model.dao;

import java.util.List;

import model.entities.Department;

public interface DepartmentDao {
	//DAO -> Data Acces Object
	void insert(Department obj);
	void update(Department obj);
	void deleteById(Integer id);
	Department findById(Integer id); //returna um department, pega o id e consulta um obj com esse id, se não existir retorna nulo
	List<Department> findAll();
	

}
