package com.rapidexpress.view;

import com.rapidexpress.controller.ConductorController;
import com.rapidexpress.controller.FlotaController;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.*;

import java.util.List;

/**
 * Vista (CLI) del modulo de personal de conduccion.
 *
 * PUNTO DE INTEGRACION: el menu principal de la Persona 3 solo debe hacer
 *     new ConductorMenuView().mostrar();
 */
public class ConductorMenuView {

    private final ConductorController controlador = new ConductorController();
    private final FlotaController flotaController = new FlotaController();

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Gestion de personal (conductores)");
            System.out.println("  1. Registrar conductor");
            System.out.println("  2. Listar todos los conductores");
            System.out.println("  3. Listar conductores por estado");
            System.out.println("  4. Consultar conductor por identificacion");
            System.out.println("  5. Actualizar datos de un conductor");
            System.out.println("  6. Cambiar estado de un conductor");
            System.out.println("  7. Asignar conductor a un vehiculo");
            System.out.println("  8. Liberar asignacion de un conductor");
            System.out.println("  9. Ver asignaciones vigentes");
            System.out.println(" 10. Eliminar conductor");
            System.out.println("  0. Volver");
            ConsolaUtil.separador();

            int opcion = ConsolaUtil.leerEntero("Opcion");
            try {
                switch (opcion) {
                    case 1 -> registrar();
                    case 2 -> listarTodos();
                    case 3 -> listarPorEstado();
                    case 4 -> consultarPorIdentificacion();
                    case 5 -> actualizar();
                    case 6 -> cambiarEstado();
                    case 7 -> asignar();
                    case 8 -> liberar();
                    case 9 -> listarAsignaciones();
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
        ConsolaUtil.titulo("Registrar conductor");
        String identificacion = ConsolaUtil.leerTexto("Numero de identificacion");
        String nombre = ConsolaUtil.leerTexto("Nombre completo");
        TipoLicencia licencia = seleccionarLicencia();
        String contacto = ConsolaUtil.leerTexto("Numero de contacto");
        int id = controlador.registrarConductor(identificacion, nombre, licencia, contacto);
        ConsolaUtil.exito("Conductor registrado con id " + id + ".");
    }

    private void listarTodos() throws NegocioException {
        ConsolaUtil.titulo("Conductores registrados");
        imprimir(controlador.listarConductores());
    }

    private void listarPorEstado() throws NegocioException {
        EstadoConductor estado = seleccionarEstado(true);
        ConsolaUtil.titulo("Conductores en estado " + estado);
        imprimir(controlador.listarConductoresPorEstado(estado));
    }

    private void consultarPorIdentificacion() throws NegocioException {
        String identificacion = ConsolaUtil.leerTexto("Identificacion");
        Conductor c = controlador.buscarConductorPorIdentificacion(identificacion);
        if (c == null) {
            ConsolaUtil.error("No se encontro un conductor con esa identificacion.");
        } else {
            ConsolaUtil.info(c.toString());
        }
    }

    private void actualizar() throws NegocioException {
        int id = ConsolaUtil.leerEntero("Id del conductor a actualizar");
        Conductor c = controlador.buscarConductor(id);
        if (c == null) {
            ConsolaUtil.error("No existe un conductor con ese id.");
            return;
        }
        ConsolaUtil.info("Actual: " + c);
        c.setIdentificacion(ConsolaUtil.leerTexto("Nueva identificacion"));
        c.setNombreCompleto(ConsolaUtil.leerTexto("Nuevo nombre completo"));
        c.setTipoLicencia(seleccionarLicencia());
        c.setContacto(ConsolaUtil.leerTexto("Nuevo contacto"));
        controlador.actualizarConductor(c);
        ConsolaUtil.exito("Conductor actualizado.");
    }

    private void cambiarEstado() throws NegocioException {
        int id = ConsolaUtil.leerEntero("Id del conductor");
        EstadoConductor estado = seleccionarEstado(false);
        controlador.cambiarEstadoConductor(id, estado);
        ConsolaUtil.exito("Estado actualizado a " + estado + ".");
    }

    private void asignar() throws NegocioException {
        ConsolaUtil.titulo("Asignar conductor a vehiculo");
        ConsolaUtil.info("Conductores activos:");
        controlador.listarConductoresPorEstado(EstadoConductor.ACTIVO)
                .forEach(c -> ConsolaUtil.info("  " + c));
        ConsolaUtil.info("Vehiculos disponibles:");
        flotaController.listarVehiculosPorEstado(EstadoVehiculo.DISPONIBLE)
                .forEach(v -> ConsolaUtil.info("  " + v));

        int idConductor = ConsolaUtil.leerEntero("Id del conductor");
        int idVehiculo = ConsolaUtil.leerEntero("Id del vehiculo");
        controlador.asignarConductorAVehiculo(idConductor, idVehiculo);
        ConsolaUtil.exito("Asignacion creada correctamente.");
    }

    private void liberar() throws NegocioException {
        int idConductor = ConsolaUtil.leerEntero("Id del conductor");
        controlador.liberarAsignacion(idConductor);
        ConsolaUtil.exito("Asignacion liberada.");
    }

    private void listarAsignaciones() throws NegocioException {
        ConsolaUtil.titulo("Asignaciones vigentes");
        List<Asignacion> vigentes = controlador.listarAsignacionesVigentes();
        if (vigentes.isEmpty()) {
            ConsolaUtil.info("No hay asignaciones vigentes.");
            return;
        }
        vigentes.forEach(a -> ConsolaUtil.info(a.toString()));
        ConsolaUtil.info("Total: " + vigentes.size());
    }

    private void eliminar() throws NegocioException {
        int id = ConsolaUtil.leerEntero("Id del conductor a eliminar");
        if (ConsolaUtil.confirmar("Confirma la eliminacion")) {
            controlador.eliminarConductor(id);
            ConsolaUtil.exito("Conductor eliminado.");
        } else {
            ConsolaUtil.info("Operacion cancelada.");
        }
    }

    private TipoLicencia seleccionarLicencia() {
        ConsolaUtil.info("Categorias: A1 A2 B1 B2 B3 C1 C2 C3");
        while (true) {
            try {
                return TipoLicencia.desdeTexto(ConsolaUtil.leerTexto("Tipo de licencia"));
            } catch (IllegalArgumentException e) {
                ConsolaUtil.error(e.getMessage());
            }
        }
    }

    /** @param incluirEnRuta true al filtrar listados, false al cambiar estado manualmente. */
    private EstadoConductor seleccionarEstado(boolean incluirEnRuta) {
        System.out.println("  1. Activo   2. De Vacaciones   3. Inactivo" + (incluirEnRuta ? "   4. En Ruta" : ""));
        return switch (ConsolaUtil.leerEntero("Estado")) {
            case 2 -> EstadoConductor.DE_VACACIONES;
            case 3 -> EstadoConductor.INACTIVO;
            case 4 -> incluirEnRuta ? EstadoConductor.EN_RUTA : EstadoConductor.ACTIVO;
            default -> EstadoConductor.ACTIVO;
        };
    }

    private void imprimir(List<Conductor> conductores) {
        if (conductores.isEmpty()) {
            ConsolaUtil.info("No hay conductores que mostrar.");
            return;
        }
        conductores.forEach(c -> ConsolaUtil.info(c.toString()));
        ConsolaUtil.info("Total: " + conductores.size());
    }
}
