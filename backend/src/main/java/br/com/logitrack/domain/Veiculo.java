package br.com.logitrack.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVeiculo tipo;

    private Integer ano;

    protected Veiculo() { }

    public Veiculo(String placa, String modelo, TipoVeiculo tipo, Integer ano) {
        atualizar(placa, modelo, tipo, ano);
    }

    public void atualizar(String placa, String modelo, TipoVeiculo tipo, Integer ano) {
        this.placa = normalizarPlaca(placa);
        this.modelo = modelo.trim();
        this.tipo = tipo;
        this.ano = ano;
    }

    /** Formato único no banco: abc1234 -> ABC-1234; Mercosul abc1d23 -> ABC1D23. */
    public static String normalizarPlaca(String placa) {
        String p = placa.trim().toUpperCase().replace("-", "");
        boolean antiga = Character.isDigit(p.charAt(4));
        return antiga ? p.substring(0, 3) + "-" + p.substring(3) : p;
    }

    public Integer getId() { return id; }
    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public TipoVeiculo getTipo() { return tipo; }
    public Integer getAno() { return ano; }
}
