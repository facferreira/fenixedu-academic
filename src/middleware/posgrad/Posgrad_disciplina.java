
// Generated by impart OJB Generator
// www.impart.ch matthias.roth@impart.ch

package middleware.posgrad;


public class Posgrad_disciplina  
{
  private long codigocursomestrado;

  private long codigointerno;

  private String nome;
  
  private Double creditos;
  private String sigla;
  private String tipo;
  private String optativa;
  private Integer codigoCurricularCourse;

  public long getCodigocursomestrado()
  {
     return this.codigocursomestrado;
  }
  public void setCodigocursomestrado(long param)
  {
    this.codigocursomestrado = param;
  }


  public long getCodigointerno()
  {
     return this.codigointerno;
  }
  public void setCodigointerno(long param)
  {
    this.codigointerno = param;
  }


  public String getNome()
  {
     return this.nome;
  }
  public void setNome(String param)
  {
    this.nome = param;
  }


  public String toString(){
    return  " [codigoCursoMestrado] " + codigocursomestrado + " [codigoInterno] " + codigointerno + " [nome] " + nome;

  }
/**
 * @return
 */
public Double getCreditos() {
	return creditos;
}

/**
 * @return
 */
public String getSigla() {
	return sigla;
}

/**
 * @return
 */
public String getTipo() {
	return tipo;
}

/**
 * @param double1
 */
public void setCreditos(Double double1) {
	creditos = double1;
}

/**
 * @param string
 */
public void setSigla(String string) {
	sigla = string;
}

/**
 * @param string
 */
public void setTipo(String string) {
	tipo = string;
}

/**
 * @return
 */
public Integer getCodigoCurricularCourse() {
	return codigoCurricularCourse;
}

/**
 * @param integer
 */
public void setCodigoCurricularCourse(Integer integer) {
	codigoCurricularCourse = integer;
}

/**
 * @return
 */
public String getOptativa() {
	return optativa;
}

/**
 * @param string
 */
public void setOptativa(String string) {
	optativa = string;
}

}

