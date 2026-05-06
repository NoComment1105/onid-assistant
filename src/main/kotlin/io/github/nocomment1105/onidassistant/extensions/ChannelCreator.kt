package io.github.nocomment1105.onidassistant.extensions

import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createNewsChannel
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.behavior.createCategory
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalRole
import dev.kordex.core.commands.converters.impl.role
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI

class ChannelCreator : Extension() {
	override val name: String = "channel-creator"

	@OptIn(UnsafeAPI::class)
	override suspend fun setup() {
		ephemeralSlashCommand(::ChannelsArgs) {
			name = "onid-channels".toKey()
			description = "Create the channels required for an oNiD event/series".toKey()

			check {
				anyGuild()
				hasPermission(Permission.ManageChannels)
			}

			action {
				val permissions = mutableListOf(
					Overwrite(
						guild!!.id,
						OverwriteType.Role,
						Permissions(),
						Permissions(Permission.ViewChannel)
					),
					Overwrite(
						arguments.eventRole.id,
						OverwriteType.Role,
						Permissions(Permission.ViewChannel),
						Permissions()
					),
					Overwrite(
						arguments.adminRole.id,
						OverwriteType.Role,
						Permissions(Permission.ViewChannel),
						Permissions()
					)
				)
				// Add the extra role if it exists, if not just go about business as usual.
				if (arguments.extraRole != null) {
					permissions.add(
						Overwrite(
							arguments.extraRole!!.id,
							OverwriteType.Role,
							Permissions(Permission.ViewChannel),
							Permissions()
						)
					)
				}

				channels(
					guild,
					arguments.categoryName,
					arguments.channelPrefix,
					arguments.eventRole,
					permissions
				)

				respond { content = "Created channels!" }
			}
		}
	}

	class ChannelsArgs : Arguments() {
		val categoryName by string {
			name = "category-name".toKey()
			description = "The name to give the category".toKey()
		}

		val channelPrefix by string {
			name = "channel-prefix".toKey()
			description = "The championship prefix to give to channels".toKey()
		}

		/** The role required to see the channels for the event. */
		val eventRole by role {
			name = "event-role".toKey()
			description = "The role users must have to see the channels".toKey()
		}

		/** The role for the admins that need to see the channels. */
		val adminRole by role {
			name = "admin-role".toKey()
			description = "The role for the admins that need to see the channels".toKey()
		}

		/** An optional extra role that may need access to the channels in the category. */
		val extraRole by optionalRole {
			name = "extra-role".toKey()
			description = "An optional extra role that may need to see these channels.".toKey()
		}
	}

	/**
	 * Creates all the channels within the specified [category] for an oNiD Event.
	 *
	 * @param prefix The name of the event to put into the channel names
	 * @param eventRole The [Role] users will require to see these channels.
	 *  Used to deny [Send Messages][Permission.SendMessages] in certain channels.
	 * @param permissions The permission overwrites for the channels
	 */
	private suspend fun channels(
		guild: GuildBehavior?,
		categoryName: String,
		prefix: String,
		eventRole: Role,
		permissions: List<Overwrite>
	) {
		if (guild == null) return
		val category = guild.createCategory(categoryName) {
			permissions.forEach { addOverwrite(it) }
		}

		// \uFE31 = Vertical dash to separate emoji from channel name.
		category.createNewsChannel("\uD83D\uDEA8\uFE31$prefix-announcements").denySendMessages(eventRole.id)
		category.createTextChannel("\uD83D\uDCAC\uFE31$prefix-chat")
		category.createTextChannel("\uD83C\uDFC1\uFE31$prefix-incident-outcomes").denySendMessages(eventRole.id)
		category.createVoiceChannel("$prefix VC")
	}

	/**
	 * Denies the [Send Messages][Permission.SendMessages] permission from the given [roleId].
	 *
	 * @param roleId The ID of the role to deny the permissions to
	 */
	private suspend fun <T : CategorizableChannel> T.denySendMessages(roleId: Snowflake) {
		this.editRolePermission(roleId) {
			allowed = Permissions(Permission.ViewChannel)
			denied = Permissions(Permission.SendMessages)
		}
	}
}
