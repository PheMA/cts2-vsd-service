DROP DATABASE IF EXISTS valuesets;
GO

CREATE DATABASE valuesets;
GO

GRANT ALL ON valuesets.* to 'cts2'@'127.0.0.1' WITH GRANT OPTION;
GO





USE valuesets
GO
/****** Object:  Table [dbo].[ValueSetVersion_includesValueSets]    Script Date: 02/19/2014 14:47:50 ******/
CREATE TABLE [dbo].[ValueSetVersion_includesValueSets](
	[ValueSetVersion_documentUri] [varchar](255) NOT NULL,
	[includesValueSets] [varchar](255) NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ValueSetEntry]    Script Date: 02/19/2014 14:47:50 ******/
CREATE TABLE [dbo].[ValueSetEntry](
	[id] [varchar](255) NOT NULL,
	[code] [varchar](255) NULL,
	[codeSystem] [varchar](255) NULL,
	[codeSystemVersion] [varchar](255) NULL,
	[description] [varchar](1024) NULL,
	[valueSetVersion_documentUri] [varchar](255) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ValueSetChange_ValueSetVersion]    Script Date: 02/19/2014 14:47:50 ******/
CREATE TABLE [dbo].[ValueSetChange_ValueSetVersion](
	[ValueSetChange_changeSetUri] [varchar](255) NOT NULL,
	[versions_documentUri] [varchar](255) NOT NULL,
UNIQUE NONCLUSTERED
(
	[versions_documentUri] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ValueSetChange]    Script Date: 02/19/2014 14:47:50 ******/
CREATE TABLE [dbo].[ValueSetChange](
	[changeSetUri] [varchar](255) NOT NULL,
	[closeDate] [datetime2](7) NULL,
	[creator] [varchar](255) NULL,
	[date] [datetime2](7) NULL,
	[instructions] [varchar](255) NULL,
	[state] [int] NULL,
	[currentVersion_documentUri] [varchar](255) NULL,
PRIMARY KEY CLUSTERED
(
	[changeSetUri] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ValueSet_ValueSetVersion]    Script Date: 02/19/2014 14:47:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ValueSet_ValueSetVersion](
	[ValueSet_name] [varchar](255) NOT NULL,
	[versions_documentUri] [varchar](255) NOT NULL,
UNIQUE NONCLUSTERED
(
	[versions_documentUri] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ValueSet]    Script Date: 02/19/2014 14:47:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ValueSet](
	[name] [varchar](255) NOT NULL,
	[formalName] [varchar](255) NULL,
	[href] [varchar](255) NULL,
	[uri] [varchar](255) NULL,
	[currentVersion_documentUri] [varchar](255) NULL,
PRIMARY KEY CLUSTERED
(
	[name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ValueSetVersion]    Script Date: 02/19/2014 14:47:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ValueSetVersion](
	[documentUri] [varchar](255) NOT NULL,
	[binding] [varchar](255) NULL,
	[changeCommitted] [varchar](255) NULL,
	[changeSetUri] [varchar](255) NULL,
	[changeType] [varchar](255) NULL,
	[creator] [varchar](255) NULL,
	[notes] [varchar](255) NULL,
	[prevChangeSetUri] [varchar](255) NULL,
	[qdmCategory] [varchar](255) NULL,
	[revisionDate] [datetime2](7) NULL,
	[source] [varchar](255) NULL,
	[state] [varchar](255) NOT NULL,
	[status] [varchar](255) NULL,
	[successor] [varchar](255) NULL,
	[synopsis] [varchar](255) NULL,
	[valueSetType] [varchar](255) NULL,
	[version] [varchar](255) NULL,
	[valueSet_name] [varchar](255) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[documentUri] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ValueSet_ValueSetProperty]    Script Date: 02/19/2014 14:47:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ValueSet_ValueSetProperty](
	[ValueSet_name] [varchar](255) NOT NULL,
	[properties_id] [varchar](255) NOT NULL,
UNIQUE NONCLUSTERED
(
	[properties_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ValueSetProperty]    Script Date: 02/19/2014 14:47:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ValueSetProperty](
	[id] [varchar](255) NOT NULL,
	[name] [varchar](255) NULL,
	[value] [varchar](1024) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ValueSetProperty_qualifiers]    Script Date: 02/19/2014 14:47:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ValueSetProperty_qualifiers](
	[ValueSetProperty_id] [varchar](255) NOT NULL,
	[qualName] [varchar](255) NULL,
	[qualValue] [varchar](255) NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  ForeignKey [FKAFCCD55113E71833]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSet]  WITH CHECK ADD  CONSTRAINT [FKAFCCD55113E71833] FOREIGN KEY([currentVersion_documentUri])
REFERENCES [dbo].[ValueSetVersion] ([documentUri])
GO
ALTER TABLE [dbo].[ValueSet] CHECK CONSTRAINT [FKAFCCD55113E71833]
GO
/****** Object:  ForeignKey [FKAA80EF74469B2FEF]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSet_ValueSetProperty]  WITH CHECK ADD  CONSTRAINT [FKAA80EF74469B2FEF] FOREIGN KEY([ValueSet_name])
REFERENCES [dbo].[ValueSet] ([name])
GO
ALTER TABLE [dbo].[ValueSet_ValueSetProperty] CHECK CONSTRAINT [FKAA80EF74469B2FEF]
GO
/****** Object:  ForeignKey [FKAA80EF749EE5AD2]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSet_ValueSetProperty]  WITH CHECK ADD  CONSTRAINT [FKAA80EF749EE5AD2] FOREIGN KEY([properties_id])
REFERENCES [dbo].[ValueSetProperty] ([id])
GO
ALTER TABLE [dbo].[ValueSet_ValueSetProperty] CHECK CONSTRAINT [FKAA80EF749EE5AD2]
GO
/****** Object:  ForeignKey [FKBD9DB59469B2FEF]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSet_ValueSetVersion]  WITH CHECK ADD  CONSTRAINT [FKBD9DB59469B2FEF] FOREIGN KEY([ValueSet_name])
REFERENCES [dbo].[ValueSet] ([name])
GO
ALTER TABLE [dbo].[ValueSet_ValueSetVersion] CHECK CONSTRAINT [FKBD9DB59469B2FEF]
GO
/****** Object:  ForeignKey [FKBD9DB59B8AD778F]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSet_ValueSetVersion]  WITH CHECK ADD  CONSTRAINT [FKBD9DB59B8AD778F] FOREIGN KEY([versions_documentUri])
REFERENCES [dbo].[ValueSetVersion] ([documentUri])
GO
ALTER TABLE [dbo].[ValueSet_ValueSetVersion] CHECK CONSTRAINT [FKBD9DB59B8AD778F]
GO
/****** Object:  ForeignKey [FK6B2DD46113E71833]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSetChange]  WITH CHECK ADD  CONSTRAINT [FK6B2DD46113E71833] FOREIGN KEY([currentVersion_documentUri])
REFERENCES [dbo].[ValueSetVersion] ([documentUri])
GO
ALTER TABLE [dbo].[ValueSetChange] CHECK CONSTRAINT [FK6B2DD46113E71833]
GO
/****** Object:  ForeignKey [FK45FABA69B8AD778F]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSetChange_ValueSetVersion]  WITH CHECK ADD  CONSTRAINT [FK45FABA69B8AD778F] FOREIGN KEY([versions_documentUri])
REFERENCES [dbo].[ValueSetVersion] ([documentUri])
GO
ALTER TABLE [dbo].[ValueSetChange_ValueSetVersion] CHECK CONSTRAINT [FK45FABA69B8AD778F]
GO
/****** Object:  ForeignKey [FK45FABA69E7D3BF3E]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSetChange_ValueSetVersion]  WITH CHECK ADD  CONSTRAINT [FK45FABA69E7D3BF3E] FOREIGN KEY([ValueSetChange_changeSetUri])
REFERENCES [dbo].[ValueSetChange] ([changeSetUri])
GO
ALTER TABLE [dbo].[ValueSetChange_ValueSetVersion] CHECK CONSTRAINT [FK45FABA69E7D3BF3E]
GO
/****** Object:  ForeignKey [FK5628EDA1EF4EED5B]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSetEntry]  WITH CHECK ADD  CONSTRAINT [FK5628EDA1EF4EED5B] FOREIGN KEY([valueSetVersion_documentUri])
REFERENCES [dbo].[ValueSetVersion] ([documentUri])
GO
ALTER TABLE [dbo].[ValueSetEntry] CHECK CONSTRAINT [FK5628EDA1EF4EED5B]
GO
/****** Object:  ForeignKey [FK2B9CBCA29A1C78FF]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSetProperty_qualifiers]  WITH CHECK ADD  CONSTRAINT [FK2B9CBCA29A1C78FF] FOREIGN KEY([ValueSetProperty_id])
REFERENCES [dbo].[ValueSetProperty] ([id])
GO
ALTER TABLE [dbo].[ValueSetProperty_qualifiers] CHECK CONSTRAINT [FK2B9CBCA29A1C78FF]
GO
/****** Object:  ForeignKey [FKE3767247469B2FEF]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSetVersion]  WITH CHECK ADD  CONSTRAINT [FKE3767247469B2FEF] FOREIGN KEY([valueSet_name])
REFERENCES [dbo].[ValueSet] ([name])
GO
ALTER TABLE [dbo].[ValueSetVersion] CHECK CONSTRAINT [FKE3767247469B2FEF]
GO
/****** Object:  ForeignKey [FKE1F340DFEF4EED5B]    Script Date: 02/19/2014 14:47:50 ******/
ALTER TABLE [dbo].[ValueSetVersion_includesValueSets]  WITH CHECK ADD  CONSTRAINT [FKE1F340DFEF4EED5B] FOREIGN KEY([ValueSetVersion_documentUri])
REFERENCES [dbo].[ValueSetVersion] ([documentUri])
GO
ALTER TABLE [dbo].[ValueSetVersion_includesValueSets] CHECK CONSTRAINT [FKE1F340DFEF4EED5B]
GO
