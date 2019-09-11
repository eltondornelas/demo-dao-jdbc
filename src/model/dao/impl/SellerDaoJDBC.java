package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;
	
	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public void insert(Seller obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"INSERT INTO seller "
					+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+ "VALUES "
					+ "(?, ?, ?, ?, ?)", 
					Statement.RETURN_GENERATED_KEYS); //retorna o id do novo vendedor inserido
			
			st.setString(1,  obj.getName());
			st.setString(2,  obj.getEmail());
			st.setDate(3,  new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4,  obj.getBaseSalary());
			st.setInt(5,  obj.getDepartment().getId());
			
			int rowsAffected = st.executeUpdate();
			
			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1); //primeira coluna da chave gerada
					obj.setId(id);					
				}
				DB.closeResultSet(rs);
			}
			else {
				throw new DbException("Unexpected error! No rows affected!"); //se não executou a inserção, alguma coisa de errado aconteceu
			}
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			
		}
		
	}

	@Override
	public void update(Seller obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"UPDATE seller "
					+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
					+ "WHERE Id = ?"); //retorna o id do novo vendedor inserido
			
			st.setString(1,  obj.getName());
			st.setString(2,  obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4,  obj.getBaseSalary());
			st.setInt(5,  obj.getDepartment().getId());
			st.setInt(6,  obj.getId());
			
			st.executeUpdate();
			
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			
		}
		
	}

	@Override
	public void deleteById(Integer id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		//não precisa criar o objeto connection com o banco, o DAO ele tem dependência com a conexão.
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "WHERE seller.Id = ?");
			
			st.setInt(1,  id);
			rs = st.executeQuery(); //executa comando SQL
			
			//ResultSet ele traz os resultados em forma de tabela, por linhas e colunas!
			//A nossa classe DAO é responsável por pegar os dados do banco de dados relacional e transformar em objetos associados.
			//Vamos criar o objeto Seller e associar ele com o Departamento.
			
			if (rs.next()) {
				//instanciar departamento
				Department dep = instantiateDepartment(rs);
				
				//instanciar o vendedor
				Seller obj = instantiateSeller(rs, dep);
				
				return obj;
			}
			
			return null; //não existia nenhum vendedor com tal id
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);			
		}
	}

	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {  //propagar a exceção
		Seller obj = new Seller();
		
		obj.setId(rs.getInt("Id"));
		obj.setName(rs.getString("Name"));
		obj.setEmail(rs.getString("Email"));
		obj.setBaseSalary(rs.getDouble("BaseSalary"));
		obj.setBirthDate(rs.getDate("BirthDate"));
		obj.setDepartment(dep);
		
		return obj;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException { //não há necessidade de colocar dentro de um try/catch já que a função que a chama possui, então ele propaga a exceção com o throws na chamada do método		
		Department dep = new Department();
		
		dep.setId(rs.getInt("DepartmentId")); //é o nome da coluna!
		dep.setName(rs.getString("DepName")); //abre o banco de dados ou o pdf para ver os nomes das colunas
		
		return dep;
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "					
					+ "ORDER BY Name");			
			
			rs = st.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			//Map é para que não ocorra a forma errada do exemplo do pdf.
			
			
			//pode ser 0 ou mais resultados, por isso vamos ter que criar lista e colocar em while
			while (rs.next()) {
				
				Department dep = map.get(rs.getInt("DepartmentId"));
				//se não existir, vai retornar nulo e aí sim instancia o objeto
				
				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"),  dep);
				}				
								
				Seller obj = instantiateSeller(rs, dep);
				list.add(obj);				
			}
			return list;			
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);			
		}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "WHERE DepartmentId = ? "
					+ "ORDER BY Name");
			
			st.setInt(1,  department.getId());
			
			rs = st.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			//Map é para que não ocorra a forma errada do exemplo do pdf.
			
			
			//pode ser 0 ou mais resultados, por isso vamos ter que criar lista e colocar em while
			while (rs.next()) {
				
				Department dep = map.get(rs.getInt("DepartmentId"));
				//se não existir, vai retornar nulo e aí sim instancia o objeto
				
				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"),  dep);
				}				
								
				Seller obj = instantiateSeller(rs, dep);
				list.add(obj);				
			}
			return list;			
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);			
		}
	}
	
	

}
