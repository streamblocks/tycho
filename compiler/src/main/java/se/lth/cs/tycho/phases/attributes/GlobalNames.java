package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.AbstractDecl;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

import java.util.EnumSet;
import java.util.function.Function;
import java.util.stream.Stream;

public interface GlobalNames {
	ModuleKey<GlobalNames> key = (unit, manager) ->
			MultiJ.from(Implementation.class)
					.bind("root").to(unit)
					.instance();

	GlobalEntityDecl entityDecl(QID qid, boolean includingPrivate);
	VarDecl varDecl(QID qid, boolean includingPrivate);
	TypeDecl typeDecl(QID qid, boolean includingPrivate);

	@Module
	interface Implementation extends GlobalNames {
		@Binding
		CompilationTask root();

		@Override
		default GlobalEntityDecl entityDecl(QID qid, boolean includingPrivate) {
			return find(qid, ns -> ns.getEntityDecls().stream(), includingPrivate);
		}

		@Override
		default GlobalVarDecl varDecl(QID qid, boolean includingPrivate) {
			return find(qid, ns -> ns.getVarDecls().stream(), includingPrivate);
		}

		@Override
		default TypeDecl typeDecl(QID qid, boolean includingPrivate) {
			return find(qid, ns -> ns.getTypeDecls().stream(), includingPrivate);
		}

		default <D extends GlobalDecl> D find(QID qid, Function<NamespaceDecl, Stream<D>> getDecls, boolean includingPrivate) {
			QID ns = qid.getButLast();
			String id = qid.getLast().toString();
			EnumSet<Availability> availability = includingPrivate ? pubPriv : pub;
			return root().getSourceUnits().stream()
					.map(SourceUnit::getTree)
					.filter(tree -> tree.getQID().equals(ns))
					.flatMap(getDecls)
					.filter(decl -> decl.getName().equals(id))
					.filter(decl -> availability.contains(decl.getAvailability()))
					.findFirst()
					.orElse(null);
		}

		EnumSet<Availability> pub = EnumSet.of(Availability.PUBLIC);
		EnumSet<Availability> pubPriv = EnumSet.of(Availability.PUBLIC, Availability.PRIVATE);

	}


}
