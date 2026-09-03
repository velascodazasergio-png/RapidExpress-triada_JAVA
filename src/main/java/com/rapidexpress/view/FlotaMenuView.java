package com.rapidexpress.view;

import com.rapidexpress.controller.FlotaController;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.EstadoVehiculo;
import com.rapidexpress.model.Mantenimiento;
import com.rapidexpress.model.Vehiculo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vista (CLI) del modulo de flota de vehiculos.
 *
 * PUNTO DE INTEGRACION: el menu principal de la Persona 3 solo debe hacer
 *     new FlotaMenuView().mostrar();
 * Al elegir "Volver" el control regresa al menu principal.
 */
public class FlotaMenuView {

    private final FlotaController controlador = new FlotaController();

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Gestion de flota de vehiculos");
            System.out.println("  1. Registrar vehiculo");
            System.out.println("  2. Listar todos los vehiculos");
            System.out.println("  3. Listar vehiculos por estado");
            System.out.println("  4. Consultar vehiculo por placa");
            System.out.println("  5. Actualizar datos de un vehiculo");
            System.out.println("  6. Cambiar estado de un vehiculo");
            System.out.println("  7. Registrar mantenimiento");
            System.out.println("  8. Finalizar mantenimiento");
            System.out.println("  9. Ver historial de mantenimientos de un vehiculo");
            System.out.println(" 10. Eliminar vehiculo");
            System.out.println("  0. Volver");
            ConsolaUtil.separador();

            int opcion = ConsolaUtil.leerEntero("Opcion");
            try {
                switch (opcion) {
                    case 1 -> registrar();
                    case 2 -> listarTodos();
                    case 3 -> listarPorEstado();
                    case 4 -> consultarPorPlaca();
                    case 5 -> actualizar();
                    case 6 -> cambiarEstado();
                    case 7 -> registrarMantenimiento();
                    case 8 -> finalizarMantenimiento();
                    case 9 -> historialMantenimientos();
                    case 10 -> eliminar();
                    case 0 -> salir = true;
                    default -> ConsolaUtil.error("Opcion no valida.");
                }
            } catch (NegocioException e) {
                ConsolaUtil.error(e.getMessage());
            }
            if (!salir) {
                ConsolaUtil.pausa();
            }
        }
    }

    private void registrar() throws NegocioException {
        ConsolaUtil.titulo("Registrar vehiculo");
        String placa = ConsolaUtil.leerTexto("Placa (ABC123)");
        String marca = ConsolaUtil.leerTexto("Marca");
        String modelo = ConsolaUtil.leerTexto("Modelo");
        int anio = ConsolaUtil.leerEntero("Anio de fabricacion");
        BigDecimal capacidad = ConsolaUtil.leerDecimal("Capacidad de carga (kg)");
        int id = controlador.registrarVehiculo(placa, marca, modelo, anio, capacidad);
        ConsolaUtil.exito("Vehiculo registrado con id " + id + ".");
    }

    private void listarTodos() throws NegocioException {
        ConsolaUtil.titulo("Vehiculos registrados");
        imprimir(controlador.listarVehiculos());
    }

    private void listarPorEstado() throws NegocioException {
        EstadoVehiculo estado = seleccionarEstado();
        ConsolaUtil.titulo("Vehiculos en estado " + estado);
        imprimir(controlador.listarVehiculosPorEstado(estado));
    }

    private void consultarPorPlaca() throws NegocioException {
        String placa = ConsolaUtil.leerTexto("Placa");
        Vehiculo v = controlador.buscarVehiculoPorPlaca(placa);
        if (v == null) {
            ConsolaUtil.error("No se encontro un vehiculo con la placa " + placa + ".");
        } else {
            ConsolaUtil.info(v.toString());
        }
    }

    private void actualizar() throws NegocioException {
        int id = ConsolaUtil.leerEntero("Id del vehiculo a actualizar");
        Vehiculo v = controlador.buscarVehiculo(id);
        if (v == null) {
            ConsolaUtil.error("No existe un vehiculo con ese id.");
            return;
        }
        ConsolaUtil.info("Actual: " + v);
        v.setPlaca(ConsolaUtil.leerTexto("Nueva placa"));
        v.setMarca(ConsolaUtil.leerTexto("Nueva marca"));
        v.setModelo(ConsolaUtil.leerTexto("Nuevo modelo"));
        v.setAnioFabricacion(ConsolaUtil.leerEntero("Nuevo anio"));
        v.setCapacidadCargaKg(ConsolaUtil.leerDecimal("Nueva capacidad (kg)"));
        controlador.actualizarVehiculo(v);
        ConsolaUtil.exito("Vehiculo actualizado.");
    }

    private void cambiarEstado() throws NegocioException {
        int id = ConsolaUtil.leerEntero("Id del vehiculo");
        EstadoVehiculo estado = seleccionarEstado();
        controlador.cambiarEstadoVehiculo(id, estado);
        ConsolaUtil.exito("Estado actualizado a " + estado + ".");
    }

    private void registrarMantenimiento() throws NegocioException {
        ConsolaUtil.titulo("Registrar mantenimiento");
        int idVehiculo = ConsolaUtil.leerEntero("Id del vehiculo");
        System.out.println("  1. Preventivo   2. Correctivo");
        Mantenimiento.Tipo tipo = ConsolaUtil.leerEntero("Tipo") == 2
                ? Mantenimiento.Tipo.CORRECTIVO : Mantenimiento.Tipo.PREVENTIVO;
        String descripcion = ConsolaUtil.leerTexto("Descripcion");
        var fecha = ConsolaUtil.leerFecha("Fecha de inicio");
        String taller = ConsolaUtil.leerTextoOpcional("Taller");
        int id = controlador.registrarMantenimiento(idVehiculo, tipo, descripcion, fecha, taller);
        ConsolaUtil.exito("Mantenimiento " + id + " registrado. El vehiculo quedo En Mantenimiento.");
    }

    private void finalizarMantenimiento() throws NegocioException {
        ConsolaUtil.titulo("Mantenimientos en curso");
        List<Mantenimiento> abiertos = controlador.listarMantenimientosAbiertos();
        if (abiertos.isEmpty()) {
            ConsolaUtil.info("No hay mantenimientos en curso.");
            return;
        }
        abiertos.forEach(m -> ConsolaUtil.info(m.toString()));
        int id = ConsolaUtil.leerEntero("Id del mantenimiento a finalizar");
        var fechaFin = ConsolaUtil.leerFecha("Fecha de finalizacion");
        BigDecimal costo = ConsolaUtil.leerDecimal("Costo total");
        controlador.finalizarMantenimiento(id, fechaFin, costo);
        ConsolaUtil.exito("Mantenimiento finalizado. El vehiculo volvio a Disponible.");
    }

    private void historialMantenimientos() throws NegocioException {
        int idVehiculo = ConsolaUtil.leerEntero("Id del vehiculo");
        List<Mantenimiento> historial = controlador.historialMantenimientos(idVehiculo);
        ConsolaUtil.titulo("Historial de mantenimientos");
        if (historial.isEmpty()) {
            ConsolaUtil.info("El vehiculo no tiene mantenimientos registrados.");
            return;
        }
        historial.forEach(m -> ConsolaUtil.info(m.toString()));
    }

    private void eliminar() throws NegocioException {
        int id = ConsolaUtil.leerEntero("Id del vehiculo a eliminar");
        if (ConsolaUtil.confirmar("Esta seguro? Se borrara tambien su historial de mantenimientos")) {
            controlador.eliminarVehiculo(id);
            ConsolaUtil.exito("Vehiculo eliminado.");
        } else {
            ConsolaUtil.info("Operacion cancelada.");
        }
    }

    private EstadoVehiculo seleccionarEstado() {
        System.out.println("  1. Disponible   2. En Ruta   3. En Mantenimiento");
        return switch (ConsolaUtil.leerEntero("Estado")) {
            case 2 -> EstadoVehiculo.EN_RUTA;
            case 3 -> EstadoVehiculo.EN_MANTENIMIENTO;
            default -> EstadoVehiculo.DISPONIBLE;
        };
    }

    private void imprimir(List<Vehiculo> vehiculos) {
        if (vehiculos.isEmpty()) {
            ConsolaUtil.info("No hay vehiculos que mostrar.");
            return;
        }
        vehiculos.forEach(v -> ConsolaUtil.info(v.toString()));
        ConsolaUtil.info("Total: " + vehiculos.size());
    }
}
